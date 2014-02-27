package ca.ualberta.cs.picposter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;
import ca.ualberta.cs.picposter.model.PicPostModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ElasticSearchOperations {

	// Http Connector
	private static HttpClient httpclient = new DefaultHttpClient();

	// JSON Utilities
	private static Gson gson = new Gson();

	public static void pushPicPostModel(final PicPostModel model) {
		Thread thread = new Thread() {

			@Override
			public void run() {
				Gson gson = new Gson();
				HttpClient client = new DefaultHttpClient();
				HttpPost request = new HttpPost(
						"http://cmput301.softwareprocess.es:8080/testing/gonalves/");

				try {
					request.setEntity(new StringEntity(gson.toJson(model)));
					HttpResponse response = client.execute(request);

					Log.e("Elastic Search", response.getStatusLine().toString());

					HttpEntity entity = response.getEntity();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(entity.getContent()));

					String output = reader.readLine();
					while (output != null) {
						Log.e("Elastic Search", output);
						output = reader.readLine();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};

		thread.start();
	}

	public static ArrayList<PicPostModel> searchsearchPicPost(final String str)
			throws ClientProtocolException, IOException {
		final ArrayList<PicPostModel> searchResult = new ArrayList<PicPostModel>();
		Thread thread = new Thread() {

			@Override
			public void run() {
				try {
					HttpPost searchRequest = new HttpPost(
							"http://cmput301.softwareprocess.es:8080/testing/gonalves/_search?pretty=1");
					String query = "{\"query_string\" : {\"query\" : \""
							+ str + "\"}}";
					
					Log.i("Query", query);
					StringEntity stringentity = new StringEntity(query);

					searchRequest.setHeader("Accept", "application/json");
					searchRequest.setEntity(stringentity);

					HttpResponse response = httpclient.execute(searchRequest);
					String status = response.getStatusLine().toString();
					Log.e("ElasticSearch", status);

					String json = ElasticSearchOperations
							.getEntityContent(response);

					Type elasticSearchSearchResponseType = new TypeToken<ElasticSearchSearchResponse<PicPostModel>>() {
					}.getType();
					ElasticSearchSearchResponse<PicPostModel> esResponse = gson
							.fromJson(json, elasticSearchSearchResponseType);
					Log.e("ElasticSearch", esResponse.toString());
					for (ElasticSearchResponse<PicPostModel> r : esResponse
							.getHits()) {
						PicPostModel recipe = r.getSource();
						Log.e("ElasticSearch", recipe.toString());
						searchResult.add(recipe);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return searchResult;

	}

	static String getEntityContent(HttpResponse response) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(response.getEntity().getContent())));
		String output;
		Log.e("ElasticSearch", "Output from Server -> ");
		String json = "";
		while ((output = br.readLine()) != null) {
			Log.e("ElasticSearch", output);
			json += output;
		}
		Log.e("ElasticSearch", "JSON:" + json);
		return json;
	}

}

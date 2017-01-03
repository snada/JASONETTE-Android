package com.jasonette.seed.Action;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.jasonette.seed.Helper.JasonHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JasonOauthAction {
    public void auth(final JSONObject action, final JSONObject data, final Context context) {
        try{
            final JSONObject options = action.getJSONObject("options");
            if(options.getString("version").equals("1")) {
                //OAuth 1 - TODO
                JasonHelper.next("error", action, data, context);
            } else {
                //OAuth 2
                String view = "";

                if(options.has("view")) {
                    view = options.getString("view");
                }

                JSONObject access_options = null;
                if(options.has("access")) {
                    access_options = options.getJSONObject("access");
                }

                if(access_options != null && access_options.has("data") && access_options.getJSONObject("data").getString("grant_type").equals("password")) {
                    //Password auth - TODO
                    JasonHelper.next("error", action, data, context);
                } else {
                    //Assuming code auth
                    JSONObject authorize_options = options.getJSONObject("authorize");

                    if(authorize_options == null || authorize_options.length() == 0) {
                        JasonHelper.next("error", action, data, context);
                    } else {
                        String client_id = authorize_options.getString("client_id");
                        String client_secret = "";

                        //Secret could not be used
                        if(authorize_options.has("client_secret")) {
                            client_secret = authorize_options.getString("client_secret");
                        }

                        if(!authorize_options.has("scheme") || authorize_options.getString("scheme").length() == 0
                            || !authorize_options.has("host") || authorize_options.getString("host").length() == 0
                            || !authorize_options.has("path") || authorize_options.getString("path").length() == 0
                        ) {
                            JasonHelper.next("error", action, data, context);
                        } else {
                            // TODO
                            //CHECK IF CREDENTIALS EXISTS
                            //REFRESH ACCESS TOKEN IF THAT IS THE CASE

                            Uri.Builder builder = new Uri.Builder();

                            builder.scheme(authorize_options.getString("scheme"))
                                    .authority(authorize_options.getString("host"));

                            for(String fragment: authorize_options.getString("path").split("/")) {
                                if(!fragment.equals("")) {
                                    builder.appendPath(fragment);
                                }
                            }

                            builder.appendQueryParameter("client_id", client_id);
                            if(client_secret.length() > 0) {
                                builder.appendQueryParameter("client_secret", client_secret);
                            }

                            JSONObject options_data = authorize_options.getJSONObject("data");
                            Iterator<?> keys =  options_data.keys();
                            while( keys.hasNext() ) {
                                String key = (String)keys.next();
                                builder.appendQueryParameter(key, options_data.getString(key));
                            }

                            Uri uri = builder.build();

                            if(view.equals("app")) {
                                //error here
                            } else {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(uri);
                                context.startActivity(intent);
                            }
                        }
                    }
                }
            }
        } catch(JSONException e)  {
            try {
                JSONObject error = new JSONObject();
                error.put("data", e.toString());
                JasonHelper.next("error", action, error, context);
            } catch(JSONException error) {
                Log.d("Error", error.toString());
            }
        }
    }

    public void access_token(final JSONObject action, final JSONObject data, final Context context) {
        try {
            final JSONObject options = action.getJSONObject("options");
            if (options.getString("version").equals("1")) {
                //OAuth 1 - TODO
            } else {
                //OAuth 2
                String client_id = options.getJSONObject("access").getString("client_id");

                SharedPreferences sharedPreferences = context.getSharedPreferences("oauth", Context.MODE_PRIVATE);
                String access_token = sharedPreferences.getString(client_id, null);
                if(access_token != null) {
                    JSONObject result = new JSONObject();
                    result.put("token", access_token);
                    JasonHelper.next("success", action, result, context);
                } else {
                    JSONObject error = new JSONObject();
                    error.put("data", "access token not found");
                    JasonHelper.next("error", action, error, context);
                }
            }
        } catch(JSONException e) {
            try {
                JSONObject error = new JSONObject();
                error.put("data", e.toString());
                JasonHelper.next("error", action, error, context);
            } catch(JSONException error) {
                Log.d("Error", error.toString());
            }
        }
    }

    public void oauth_callback(final JSONObject action, final JSONObject data, final Context context) {
        try {
            final JSONObject options = action.getJSONObject("options");
            if (options.has("version") && options.getString("version").equals("1")) {
                //OAuth 1
                JasonHelper.next("error", action, data, context);
            } else {
                //OAuth 2
                //Assumption that uri will be passed in data
                Uri uri = Uri.parse(data.getString("uri"));

                String access_token = uri.getQueryParameter("access_token"); // get access token from url here

                if (access_token.length() > 0) {
                    String client_id = options.getJSONObject("authorize").getString("client_id");

                    SharedPreferences preferences = context.getSharedPreferences("oauth", Context.MODE_PRIVATE);
                    preferences.edit().putString(client_id, access_token).apply();

                    JSONObject result = new JSONObject();
                    result.put("token", access_token);

                    JasonHelper.next("success", action, result, context);
                } else {

                    JSONObject access_options = options.getJSONObject("access");
                    JSONObject access_data = access_options.getJSONObject("data");

                    String client_id = access_options.getString("client_id");
                    String client_secret = access_options.getString("client_secret");

                    String code = ""; //extract code from url

                    if (access_options.length() == 0
                        || !access_options.has("scheme") || access_options.getString("scheme").length() == 0
                        || !access_options.has("host") || access_options.getString("host").length() == 0
                        || !access_options.has("path") || access_options.getString("path").length() == 0
                    ) {
                        JasonHelper.next("error", action, data, context);
                    } else {
                        //Exchange code with access_token - TODO
                    }
                }
            }
        }
        catch(JSONException e) {
            try {
                JSONObject error = new JSONObject();
                error.put("data", e.toString());
                JasonHelper.next("error", action, error, context);
            } catch(JSONException error) {
                Log.d("Error", error.toString());
            }
        }
    }

    public void reset(final JSONObject action, final JSONObject data, final Context context) {
        try {
            final JSONObject options = action.getJSONObject("options");

            String client_id = options.getString("client_id");

            if(options.has("version") && options.getString("version").equals("1")) {
                //TODO
            } else {
                SharedPreferences preferences = context.getSharedPreferences("oauth", Context.MODE_PRIVATE);
                preferences.edit().remove(client_id).apply();
                JasonHelper.next("success", action, data, context);
            }
        } catch(JSONException e) {
            try {
                JSONObject error = new JSONObject();
                error.put("data", e.toString());
                JasonHelper.next("error", action, error, context);
            } catch(JSONException error) {
                Log.d("Error", error.toString());
            }
        }
    }

    public void request(final JSONObject action, final JSONObject data, final Context context) {

    }
}


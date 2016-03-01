package onl.deepspace.wgs;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements OnTaskCompletedInterface {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String savedPw = Helper.getPw(this);
        String savedEmail = Helper.getEmail(this);

        if(!(savedPw.equals("") && savedEmail.equals(""))) {
            new GetUserData(LoginActivity.this).execute(savedEmail, savedPw);
            setContentView(R.layout.activity_loading);
        } else {
            setContentView(R.layout.activity_login);

            TextView loginhint = (TextView) findViewById(R.id.loginhint);
            loginhint.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());

            Button button = (Button) findViewById(R.id.email_sign_in_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                String email = ((TextView) findViewById(R.id.email)).getText().toString();
                String pw = ((TextView) findViewById(R.id.password)).getText().toString();

                login(email, pw);
                }
            });
        }
    }

    public void login(String email, String pw) {
        findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
        new GetUserData(LoginActivity.this).execute(email, pw);
    }

    @Override
    public void onTaskCompleted(String response) {
        Helper.setApiResult(this, response);
        Log.d(Helper.LOGTAG, response);
        try{
            JSONObject arr = new JSONObject(response);

            Log.d(Helper.LOGTAG, Boolean.toString(arr.getBoolean(Helper.API_RESULT_LOGIN)));
            if(arr.getBoolean(Helper.API_RESULT_LOGIN)) {
                CheckBox saveLoginBox = (CheckBox) findViewById(R.id.saveLogin);
                if(saveLoginBox != null) {
                    Boolean saveLogin = saveLoginBox.isChecked();
                    if (saveLogin) {
                        Helper.setPw(getBaseContext(), ((TextView) findViewById(R.id.password)).getText().toString());
                        Helper.setEmail(getBaseContext(), ((TextView) findViewById(R.id.email)).getText().toString());
                    }
                }

                Intent intent = new Intent(this, PortalActivity.class);
                intent.putExtra(Helper.API_RESULT_CHILDREN,
                        arr.getJSONArray(Helper.API_RESULT_CHILDREN).toString());
                startActivity(intent);
            }
            else{
                setContentView(R.layout.activity_login);
                Snackbar.make(
                        findViewById(R.id.login_activity),
                        arr.getString("error"),
                        Snackbar.LENGTH_SHORT).show();
            }
        }
        catch(JSONException e){
            setContentView(R.layout.activity_login);
            Toast.makeText(LoginActivity.this, R.string.connection_failed, Toast.LENGTH_SHORT).show();
            Log.e(Helper.LOGTAG, e.toString());
        }
    }

    public class GetUserData extends AsyncTask<String, Void, String> {

        private OnTaskCompletedInterface taskCompleted;

        @Override
        protected String doInBackground(String... params) {
            return Helper.GetSomething(params[0], params[1], false);
        }

        public GetUserData(OnTaskCompletedInterface activityContext) {
            this.taskCompleted = activityContext;
        }

        protected void onPostExecute(String page) {
            taskCompleted.onTaskCompleted(page);
        }

    }
}

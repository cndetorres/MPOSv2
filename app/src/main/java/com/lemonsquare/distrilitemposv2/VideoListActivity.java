package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class VideoListActivity extends Activity {

    DBController controller = new DBController(this);
    ListView VLDetails;
    ArrayList<HashMap<String, String>> hmVLDetails;
    ListAdapter laVLDetails;
    HashMap<String, String> mVLDetail;
    ArrayList<String> sVLDetails;
    String path;
    int currVideoPosition = 0;
    VideoView vvideo;
    Boolean isVideoPaused;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videolist);

        VLDetails = (ListView) findViewById(R.id.lvVLDetail) ;

        File videopath = new File(controller.videos);
        File[] strFileName =  videopath.listFiles();

        try {
            sVLDetails = new ArrayList<String>();
            for (int i = 0; i < strFileName.length;i++){
                if (strFileName[i].getName().endsWith(".mp4")){
                    sVLDetails.add(strFileName[i].getName());
                }
            }
        }catch (Exception e){
            Toasty.error(this, "no defined file path", Toast.LENGTH_LONG).show();
        }


        ViewListview();

        VLDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                path = controller.videos + "/" + hmVLDetails.get(position).get("Header");

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                intent.setDataAndType(Uri.parse(path), "video/mp4");
                startActivity(intent);

                //Playvideo();

            }
        });

    }

    public void Playvideo() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(VideoListActivity.this);
        LayoutInflater inflater = VideoListActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_viewvideo, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);

        final TextView tvvlPause = (TextView) dialogView.findViewById(R.id.tvvlPause);
        final TextView tvvlResume = (TextView) dialogView.findViewById(R.id.tvvlResume);
        final TextView tvvlReplay = (TextView) dialogView.findViewById(R.id.tvvlReplay);
        vvideo = (VideoView) dialogView.findViewById(R.id.vlViewvideo);
        vvideo.setVideoPath(path);
        vvideo.start();


        currVideoPosition = 0;
        tvvlResume.setTextColor(Color.RED);
        tvvlReplay.setTextColor(Color.RED);
        tvvlResume.setEnabled(false);
        tvvlReplay.setEnabled(false);

        //btnTTimein.setBackgroundColor(Color.RED);
        //btnTTimeout.setBackgroundColor(Color.parseColor("#008000"));

        vvideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            public void onCompletion(MediaPlayer mp)
            {
                tvvlReplay.setEnabled(true);
                tvvlReplay.setTextColor(Color.parseColor("#008000"));

            }
        });

        tvvlPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vvideo.pause();
                isVideoPaused = true;
                currVideoPosition = vvideo.getCurrentPosition();
                setContinueVideoAfterSeekComplete();
                tvvlResume.setEnabled(true);
                tvvlPause.setEnabled(false);
                tvvlResume.setTextColor(Color.parseColor("#008000"));
                tvvlPause.setTextColor(Color.RED);
            }
        });

        tvvlResume.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vvideo.seekTo(currVideoPosition);
                setContinueVideoAfterSeekComplete();
                tvvlResume.setEnabled(false);
                tvvlPause.setEnabled(true);
                tvvlResume.setTextColor(Color.parseColor("#008000"));
                tvvlPause.setTextColor(Color.RED);
            }
        });

        tvvlReplay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vvideo.start();
                tvvlReplay.setEnabled(false);
                tvvlReplay.setTextColor(Color.RED);
            }
        });


        alertDialog.show();


    }



    public void ViewListview() {

        hmVLDetails = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < sVLDetails.size(); i++) {
            mVLDetail = new HashMap<String, String>();
            mVLDetail.put("Header", sVLDetails.get(i));
            hmVLDetails.add(mVLDetail);
        }

        try {
            laVLDetails = new SimpleAdapter(this, hmVLDetails, R.layout.item_videolist,
                    new String[]{"Header"}, new int[]{
                    R.id.rowsHeader}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    if (position % 2 == 1) {
                        view.setBackgroundResource(R.color.odd);
                    } else {
                        view.setBackgroundResource(R.color.even);
                    }
                    return view;
                }
            };

            VLDetails.setAdapter(laVLDetails);

        } catch (Exception e) {

        }
    }

    private void setContinueVideoAfterSeekComplete()
    {
        vvideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mediaPlayer) {
                        if(isVideoPaused)
                        {
                            vvideo.start();
                            isVideoPaused = false;
                        }
                    }
                });
            }
        });
    }

    public void onBackPressed() {
        Intent IntentMiscellaneousActivity = new Intent(VideoListActivity.this, MiscellaneousActivity.class);
        startActivity(IntentMiscellaneousActivity);
        finish();
    }
}

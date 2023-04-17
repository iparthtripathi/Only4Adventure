package com.onlyforadventure.DocApp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.onlyforadventure.DocApp.appointment.AppointmentBooking;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class userAdapter extends RecyclerView.Adapter<userAdapter.userViewHolder> {

    Context context;
    ArrayList<userModel> list;
    SelectListener selectListener;

    public userAdapter(Context context, ArrayList<userModel> list,SelectListener selectListener) {
        this.context = context;
        this.list = list;
        this.selectListener=selectListener;
    }

    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.items,parent,false);

        return new userViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull userViewHolder holder, @SuppressLint("RecyclerView") int position) {
        userModel userModel=list.get(position);
        holder.name.setText(userModel.getName());
        holder.specialization.setText(userModel.getSpecialization());
        holder.phone.setText(userModel.getPhone());
        holder.fees.setText(userModel.getFees());
        holder.email.setText(userModel.getLoc());
        Bitmap bitmap=getBitmapFromURL(userModel.getImg());
        Picasso.get().load(userModel.getImg()).into(holder.imageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(context, AppointmentBooking.class);
                intent.putExtra("Duid", userModel.getUid());
                intent.putExtra("Dname", userModel.getName());
                intent.putExtra("Demail", userModel.getEmail());
                intent.putExtra("Dphone", userModel.getPhone());
                intent.putExtra("Dtype", userModel.getSpecialization());
                intent.putExtra("DImg",userModel.getImg());
                intent.putExtra("DLoc",userModel.getLoc());
                intent.putExtra("DFees",userModel.getFees());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class userViewHolder extends RecyclerView.ViewHolder{

        TextView name,email,specialization,phone,fees;
        CardView cardView;
        ImageView imageView;

        public userViewHolder(@NonNull View itemView) {
            super(itemView);

            name=itemView.findViewById(R.id.doctorName);
            email=itemView.findViewById(R.id.doctorEmail);
            specialization=itemView.findViewById(R.id.specialization);
            phone=itemView.findViewById(R.id.phone);
            imageView=itemView.findViewById(R.id.picture);
            cardView=itemView.findViewById(R.id.recyclerLayout);
            fees=itemView.findViewById(R.id.fees);
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {

            //uncomment below line in image name have spaces.
            //src = src.replaceAll(" ", "%20");

            URL url = new URL(src);

            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception e) {
            Log.d("vk21", e.toString());
            return null;
        }
    }
}

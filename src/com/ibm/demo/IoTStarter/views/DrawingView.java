/*******************************************************************************
 * Copyright (c) 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors: Allan Marube, Mike Robertson
 *******************************************************************************/
package com.ibm.demo.IoTStarter.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;

import com.ibm.demo.IoTStarter.IoTStarterApplication;
import com.ibm.demo.IoTStarter.utils.Constants;
import com.ibm.demo.IoTStarter.utils.MessageFactory;
import com.ibm.demo.IoTStarter.utils.MqttHandler;
import com.ibm.demo.IoTStarter.utils.TopicFactory;

/**
 * View that contains canvas to draw upon, handles all touch Events for
 * draw, drag.
 * Created by Allan Marube on 7/15/2014.
 */
public class DrawingView extends View {
    private final static String TAG = DrawingView.class.getName();
    private IoTStarterApplication app;
    private Context context;
    private Path drawPath; //user drawPath

    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    private int paintColor= Color.BLACK; //system color
    private int strokeWidth = 5; //width of pencil
    private float previousX;
    private float previousY;
    //canvas
    private Canvas drawCanvas; //canvas
    public int width = 0; //canvas width
    public int height = 0;//canvas height
    //canvas bitmap
    private Bitmap canvasBitmap;

    public DrawingView( Context context, AttributeSet attrs){
        super (context, attrs);
        setupDrawing();
    }

    /**
     * Initializes canvas and drawing classes.
     */
    private void setupDrawing() {
        //get drawing area setup for interaction
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setDither(true);
        drawPaint.setPathEffect(new CornerPathEffect(10));
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(strokeWidth);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    /**
     * Set the context for the DrawingView
     *
     * @param context The context to use.
     */
    public void setContext(Context context) {
        this.context = context;
        app = (IoTStarterApplication) context.getApplicationContext();
    }

    /**
     * Resize the view.
     *
     * @param w New view width.
     * @param h New view height.
     * @param oldw Old view width.
     * @param oldh Old view height.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //view given size
        width = w;
        height = h;
        super.onSizeChanged(w,h,oldw,oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        if (app != null) {
            colorBackground(app.getColor());
        }
    }

    /**
     *
     * @param color
     */
    public void colorBackground(int color) {
        if (drawCanvas != null) {
            // Draw white first in case alpha value is < 255
            drawCanvas.drawColor(Color.WHITE);
            drawCanvas.drawColor(color);
            invalidate();
        }
    }

    /**
     * Draw the path on the canvas.
     * @param canvas The canvas to draw on.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        drawCanvas.drawPath(drawPath, drawPaint);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
    }

    /**
     * Handle events when the user touches the screen.
     *
     * @param event The event representing the touch.
     * @return True if action is down, move or up. False otherwise.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v(TAG, ".onTouchEvent() entered");

        if (app.getConnectionType() == Constants.ConnectionType.QUICKSTART) {
            return true;
        }

        //detect user touch
        float touchX = event.getX();

        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                previousX = touchX;
                previousY = touchY;
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                publishTouchMove(touchX, touchY, false);
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                publishTouchMove(touchX, touchY, true);
                drawPath.reset();
                drawCanvas.drawColor(app.getColor());
                break;
            default:
                return false;
        }

        invalidate();

        return true;
    }

    /**
     * Publish an MQTT message of the screen touch and drag.
     *
     * @param x The ending x coordinate of the touch.
     * @param y The ending y coordinate of the touch.
     */
    private void publishTouchMove(float x, float y, boolean ended) {
        Log.v(TAG, ".publishTouchMove() entered");
        float deltaX = x - previousX;
        float deltaY = y - previousY;
        float relativeX = x / width;
        float relativeY = x / height;
        float relativeDX = deltaX / width;
        float relativeDY = deltaY / height;

        previousX = x;
        previousY = y;

        IoTStarterApplication app = (IoTStarterApplication) context.getApplicationContext();

        String messageData = MessageFactory.getTouchMessage(relativeX, relativeY, relativeDX, relativeDY, ended);

        Log.v(TAG, "Publishing touch message: " + messageData);
        MqttHandler mqttHandler = MqttHandler.getInstance(context);
        mqttHandler.publish(TopicFactory.getEventTopic(Constants.TOUCH_EVENT), messageData, false, 0);
    }
}
//
// MIT License
//
// Copyright (c) 2016-present, Critical Blue Ltd.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
// (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
// ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
// THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package io.approov.shapes.android

import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import android.util.Log
import io.approov.shapes.ApplicationApi

// *** UNCOMMENT THE LINE BELOW FOR APPROOV ***
//import io.approov.service.okhttp.ApproovService

class MainActivity: AppCompatActivity() {
    private lateinit var activity: Activity
    private lateinit var statusView: View
    private lateinit var statusImageView: ImageView
    private lateinit var statusTextView: TextView
    private lateinit var helloCheckButton: Button
    private lateinit var shapesCheckButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity = this

        // *** UNCOMMENT THE LINE BELOW FOR APPROOV ***
        //ApproovService.initialize(getApplicationContext(), "<enter-your-config-string-here>")

        // *** UNCOMMENT THE LINE BELOW FOR APPROOV USING SECRETS PROTECTION ***
        //ApproovService.addSubstitutionHeader("Api-Key", null)

        // find controls
        statusView = findViewById(R.id.viewStatus)
        statusImageView = findViewById<View>(R.id.imgStatus) as ImageView
        statusTextView = findViewById(R.id.txtStatus)
        helloCheckButton = findViewById(R.id.btnConnectionCheck)
        shapesCheckButton = findViewById(R.id.btnShapesCheck)

        // handle the hello connection check
        helloCheckButton.setOnClickListener {
            // initially hide status
            activity.runOnUiThread { statusView.visibility = View.INVISIBLE }

            // make a new Request and show the result
            ApplicationApi().hello {
                if (it == "success") {
                    Log.d(TAG, "Hello call successful")
                    statusImageView.setImageResource(R.drawable.hello)
                    statusTextView.text = "Hello call successful"
                } else {
                    Log.d(TAG, "Hello call failed")
                    statusImageView.setImageResource(R.drawable.confused)
                    statusTextView.text = "Hello call failed"
                }
                statusView.visibility = View.VISIBLE
            }
        }

        // handle getting shapes
        shapesCheckButton.setOnClickListener {
            // initially hide status
            activity.runOnUiThread { statusView.visibility = View.INVISIBLE }

            // make a new Request and show the result
            ApplicationApi().shapes {
                var imgId = R.drawable.confused
                var text = it
                if (it.equals("square", ignoreCase = true)) {
                    imgId = R.drawable.square
                    text = "Shapes call successful (square)"
                } else if (it.equals("circle", ignoreCase = true)) {
                    imgId = R.drawable.circle
                    text = "Shapes call successful (circle)"
                } else if (it.equals("rectangle", ignoreCase = true)) {
                    imgId = R.drawable.rectangle
                    text = "Shapes call successful (rectangle)"
                } else if (it.equals("triangle", ignoreCase = true)) {
                    imgId = R.drawable.triangle
                    text = "Shapes call successful (triangle)"
                }
                Log.d(TAG, text)
                statusImageView.setImageResource(imgId)
                statusTextView.text = text
                statusView.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}

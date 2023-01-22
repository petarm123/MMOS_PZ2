package com.example.projekt

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var toDoList: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private val SHAKE_THRESHOLD = 15f
    private lateinit var listView: ListView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)

        val stateList = StateListDrawable()
        stateList.addState(intArrayOf(android.R.attr.state_activated),
            ColorDrawable(ContextCompat.getColor(this, R.color.selected_item_color)))
        listView.selector = stateList



        val addButton = findViewById<Button>(R.id.addButton)

        toDoList = arrayListOf()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, toDoList)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val parentListView = parent as ListView
            parentListView.setSelector(R.color.selected_item_color)
            parentListView.setItemChecked(position, true)

        }

        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

        addButton.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_add, null)
            dialog.setView(dialogView)

            dialog.setPositiveButton("Add") { _, _ ->
                val editText = dialogView.findViewById<EditText>(R.id.editText)
                toDoList.add(editText.text.toString())
                adapter.notifyDataSetChanged()
            }

            dialog.setNegativeButton("Cancel") { _, _ -> }
            dialog.show()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == accelerometer) {
            // Handle accelerometer data here
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            if (Math.abs(x) > SHAKE_THRESHOLD || Math.abs(y) > SHAKE_THRESHOLD || Math.abs(z) > SHAKE_THRESHOLD) {
                // show an AlertDialog to confirm deletion
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Delete Item")
                builder.setMessage("Are you sure you want to delete the selected item?")
                builder.setPositiveButton("Yes"){_, _ ->
                    // check if an item is selected before trying to delete it
                    if (listView.checkedItemPosition != -1) {
                        toDoList.removeAt(listView.checkedItemPosition)
                        adapter.notifyDataSetChanged()
                    } else {
                        // Show a message to the user indicating that no item is selected
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Error")
                        builder.setMessage("No item is selected to delete.")
                        builder.setPositiveButton("OK"){_, _ -> }
                        val dialog: AlertDialog = builder.create()
                        dialog.show()
                    }
                }
                builder.setNegativeButton("No"){_, _ -> }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor == accelerometer) {
            if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                // Show a message to the user indicating that the sensor data is unreliable
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Warning")
                builder.setMessage("Accelerometer data is unreliable. Please check your device.")
                builder.setPositiveButton("OK"){_, _ -> }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }
    }

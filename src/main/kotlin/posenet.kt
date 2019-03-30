/*
 Copyright (C) 2019 Runway AI Examples

 This file is part of Runway AI Examples.

 Runway-Examples is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Runway-Examples is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with RunwayML.  If not, see <http://www.gnu.org/licenses/>.

 ===========================================================================

 RUNWAY
 www.runwayapp.ai

 PoseNet Demo
 Receive Socket.IO messages from Runway
 Made by Ryan Bateman (@ryanbateman)

 */

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import java.net.URISyntaxException

import org.json.JSONObject
import org.openrndr.Program
import org.openrndr.ffmpeg.FFMPEGVideoPlayer

fun main() = application {

    val connections:List<Pair<String, String>> = listOf(
        Pair("rightHip", "leftHip"),
        Pair("rightHip", "rightShoulder"),
        Pair("leftHip", "leftShoulder"),
        Pair("leftShoulder", "rightShoulder"),
        Pair("leftShoulder", "leftElbow"),
        Pair("leftElbow", "leftWrist"),
        Pair("rightShoulder", "rightElbow"),
        Pair("rightElbow", "rightWrist"),
        Pair("leftShoulder", "leftElbow"),
        Pair("rightHip", "rightKnee"),
        Pair("rightKnee", "rightAnkle"),
        Pair("leftHip", "leftKnee"),
        Pair("leftKnee", "leftAnkle")
    )

    //  These are the standard window size but elements are rendered in line with the source size / coordinates.
    //  i.e. if  you are using a 1024x768 source, then OpenRNDR may render elements outside of this window size and
    //  it should be adjusted with this in mind
    configure {
        width = 640
        height = 480
    }

    program {

        var json = JSONObject()
        val mSocket: Socket
        lateinit var videoPlayer: FFMPEGVideoPlayer

        val onNewMessage = object: Emitter.Listener {
            override fun call(vararg args: Any?) {
                json = args[0] as JSONObject
            }
        }

        // These are the current defaults - it's worth checking your own port settings
        try {
            mSocket = IO.socket("http://localhost:3002/")
            mSocket.on("data", onNewMessage)
            mSocket.connect()
        } catch (e: URISyntaxException) {
            println("error")
        }
        videoPlayer = FFMPEGVideoPlayer.fromDevice()
        videoPlayer.start()

        extend {
            drawer.background(ColorRGBa.BLACK)
            drawer.stroke = ColorRGBa.PINK
            drawer.strokeWeight = 1.0
            drawer.fill = ColorRGBa.PINK

            videoPlayer.next()
            videoPlayer.draw(drawer)

            if (json.has("poses")) {
                val humans: JSONArray = json.getJSONArray("poses")

                    // For each human found
                    for (j in 0..(humans.length() - 1)) {
                        val pose: JSONObject = humans.getJSONObject(j)
                        val bodyparts: JSONArray = pose.getJSONArray("keypoints")

                        // Draw the body parts
                        drawBodyParts(bodyparts)

                        // Draw the connections for the bodyparts
                        drawConnections(connections, bodyparts)
                    }
            }
        }
    }
}

private fun Program.drawConnections(connections: List<Pair<String, String>>, bodyparts: JSONArray) {
    for (connection in connections) {
        var start: JSONObject? = null
        var end: JSONObject? = null

        for (i in 0..(bodyparts?.length() - 1)) {
            val startBodypart = bodyparts.getJSONObject(i)
            if (startBodypart.getString("part") == connection.first) {
                start = startBodypart
                for (j in 0..(bodyparts?.length() - 1)) {
                    val endBodypart = bodyparts.getJSONObject(j)
                    if (endBodypart.getString("part") == connection.second) {
                        end = endBodypart
                        break
                    }
                }
                break
            }
        }

        if (start != null && end != null) {
            drawer.lineSegment(
                start.getJSONObject("position").getDouble("x"),
                start.getJSONObject("position").getDouble("y"),
                end.getJSONObject("position").getDouble("x"),
                end.getJSONObject("position").getDouble("y")
            )
        }
    }
}


private fun Program.drawBodyParts(bodyparts: JSONArray) {
    for (i in 0..(bodyparts.length() - 1)) {
        val bodypart = bodyparts.getJSONObject(i).getJSONObject("position")
        if (bodypart != null)
            drawer.circle(bodypart.getDouble("x"), bodypart.getDouble("y"), 3.0)
    }
}
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
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Vector2

fun main() = application {

    //  These are the standard window size but elements are rendered in line with the source size / coordinates.
    //  i.e. if  you are using a 1024x768 source, then OpenRNDR may render elements outside of this window size and
    //  it should be adjusted with this in mind
    configure {
        width = 640
        height = 480
    }

    program {

        // extend(ScreenRecorder())

        var json = JSONObject()
        val mSocket: Socket
        val onNewMessage = object: Emitter.Listener {
            override fun call(vararg args: Any?) {
                json = args[0] as JSONObject
            }
        }

        // These are the current defaults - it's worth checking your own port settings
        try {
            mSocket = IO.socket("http://localhost:3001/")
            mSocket.on("data", onNewMessage)
            mSocket.connect()
        } catch (e: URISyntaxException) {
            println("error")
        }

        extend {
            drawer.stroke = ColorRGBa.PINK
            drawer.strokeWeight = 1.0
            drawer.fill = ColorRGBa.PINK

            val landmarks = if (json.has("landmarks")) json.getJSONArray("landmarks") else null
            if (landmarks != null) {
                for (i in 0..landmarks.length() - 1) {
                    val person = landmarks.getJSONObject(i)
                    plotPoints(person.getJSONArray("bottom_lip"))
                    plotPoints(person.getJSONArray("top_lip"))
                    plotPoints(person.getJSONArray("chin"))
                    plotPoints(person.getJSONArray("left_eye"))
                    plotPoints(person.getJSONArray("left_eyebrow"))
                    plotPoints(person.getJSONArray("right_eye"))
                    plotPoints(person.getJSONArray("right_eyebrow"))
                    plotPoints(person.getJSONArray("nose_tip"))
                    plotPoints(person.getJSONArray("nose_bridge"))
                }
            }
        }
    }
}

private fun Program.plotPoints(feature:JSONArray) {
    val points = mutableListOf<Vector2>()
    for (i in 0..feature.length() - 1) {
        val p = feature.getJSONArray(i)
        points.add(Vector2(p.getDouble(0), p.getDouble(1)))
    }
    drawer.lineStrip(points)
}
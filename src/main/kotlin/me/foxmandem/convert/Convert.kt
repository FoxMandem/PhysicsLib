package me.foxmandem.convert

import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation
import com.jme3.math.Transform as JmeTransform

//Source: EmortalDev (?)

fun Point.jme(): Vector3f =
    Vector3f(x().toFloat(), y().toFloat(), z().toFloat())

fun Vector3f.minestom(): Vec =
    Vec(x.toDouble(), y.toDouble(), z.toDouble())

fun Vector3f.minestomPos(yaw: Float = 0f, pitch: Float = 0f): Pos =
    Pos(x.toDouble(), y.toDouble(), z.toDouble(), yaw, pitch)

fun JmeTransform.getRotationFloats(): FloatArray =
    rotation.toFloats()

fun JmeTransform.getTranslationMinestom(): Vec =
    translation.minestom()

fun JmeTransform.getTranslationMinestomPos(yaw: Float = 0f, pitch: Float = 0f): Pos =
    translation.minestomPos(yaw, pitch)

fun JmeTransform.getScaleMinestom(): Vec =
    scale.minestom()

fun JmeTransform.getRotationApache(): QuaternionRotation =
    rotation.apache()

private fun Quaternion.apache(): QuaternionRotation =
    QuaternionRotation.of(w.toDouble(), x.toDouble(), y.toDouble(), z.toDouble())

fun Quaternion.toFloats(): FloatArray =
    floatArrayOf(x, y, z, w)

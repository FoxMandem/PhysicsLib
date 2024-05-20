package me.foxmandem.shape.impl

import com.jme3.bullet.collision.shapes.HullCollisionShape
import me.foxmandem.convert.PhysUtils.getQuads
import me.foxmandem.convert.jme
import me.foxmandem.shape.Hitbox
import me.foxmandem.shape.Triangle

//Source: EmortalDev (?)

class HitboxShape(
    triangles: List<Triangle>,
) : HullCollisionShape(triangles.flatMap { listOf(it.point1.jme(), it.point2.jme(), it.point3.jme()) }) {
    constructor(box: Hitbox) : this(box.getQuads().stream().flatMap { it.triangles().stream() }.toList())
}
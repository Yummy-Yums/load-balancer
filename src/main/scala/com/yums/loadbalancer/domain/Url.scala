package com.yums.loadbalancer.domain

final case class Url(value: String) extends AnyVal:
    override def toString(): String = value
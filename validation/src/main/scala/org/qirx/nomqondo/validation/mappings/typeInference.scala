package org.qirx.nomqondo.validation.mappings

import shapeless.HList
import shapeless.ops.hlist.ToList
import shapeless.ops.hlist.Mapper
import shapeless.Poly1
import scala.annotation.implicitNotFound

/**
 * A trait that can be implicitly resolved to determine the
 * key and input type based on the key mappings inside an
 * HList
 */
@implicitNotFound("Could not determine key and input types from given set of key mappings. Make sure all key types and all input types are the same.")
trait KeyAndInputTypesFrom[KeyMappings <: HList, K, I]

object KeyAndInputTypesFrom {

  implicit def forMappingTree[KeyMappings <: HList, K, I, WithoutOutput <: HList, MappingType](
    implicit removeOutputType: Mapper.Aux[keyInputTypes.type, KeyMappings, WithoutOutput],
    commonParent: ToList[WithoutOutput, MappingType],
    extractKeyAndInputTypes: MappingType <:< KeyMapping[K, I, _]) =
    new KeyAndInputTypesFrom[KeyMappings, K, I] {}

  object keyInputTypes extends Poly1 {
    implicit def all[K, I, M](
      implicit ev: M => KeyMapping[K, I, _]) =
      at[M] { mapping =>
        ev(mapping)
      }
  }
}


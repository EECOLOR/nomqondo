package org.qirx.nomqondo.validation

trait DataProviderTree[K, V] {
  /**
   * A key can have three states:
   *
   * 1. It does not exist
   * 2. It is a value
   * 3. It is another data provider
   */
  def get(key: K): Option[Either[V, DataProviderTree[K, V]]]
}

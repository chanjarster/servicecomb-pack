/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.saga.core;

class RequestProcessTask implements SagaTask {

  private final EventStore eventStore;
  private final long id;
  private final SagaRequest request;

  RequestProcessTask(long id, SagaRequest request, EventStore eventStore) {
    this.id = id;
    this.request = request;
    this.eventStore = eventStore;
  }

  @Override
  public long id() {
    return id;
  }

  @Override
  public Operation transaction() {
    return request.transaction();
  }

  @Override
  public void commit() {
    eventStore.offer(new TransactionStartedEvent(this));
    request.commit();
    eventStore.offer(new TransactionEndedEvent(this));
  }

  @Override
  public void compensate() {
    eventStore.offer(new CompensationStartedEvent(this));
    request.compensate();
    eventStore.offer(new CompensationEndedEvent(this));
  }

  @Override
  public void abort(Exception e) {
    eventStore.offer(new TransactionAbortedEvent(this, e));
  }

  @Override
  public Operation compensation() {
    return request.compensation();
  }
}
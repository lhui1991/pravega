/**
 * Copyright (c) Dell Inc., or its subsidiaries. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.pravega.shared.controller.event;

import io.pravega.common.ObjectBuilder;
import io.pravega.common.io.serialization.RevisionDataInput;
import io.pravega.common.io.serialization.RevisionDataOutput;
import io.pravega.common.io.serialization.VersionedSerializer;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UpdateStreamEvent implements ControllerEvent {
    private static final long serialVersionUID = 1L;
    private final String scope;
    private final String stream;
    private final long requestId;

    @Override
    public String getKey() {
        return String.format("%s/%s", scope, stream);
    }

    @Override
    public CompletableFuture<Void> process(RequestProcessor processor) {
        return processor.processUpdateStream(this);
    }

    //region Serialization

    private static class UpdateStreamEventBuilder implements ObjectBuilder<UpdateStreamEvent> {
    }

    static class Serializer extends VersionedSerializer.WithBuilder<UpdateStreamEvent, UpdateStreamEventBuilder> {
        @Override
        protected UpdateStreamEventBuilder newBuilder() {
            return UpdateStreamEvent.builder();
        }

        @Override
        protected byte getWriteVersion() {
            return 0;
        }

        @Override
        protected void declareVersions() {
            version(0).revision(0, this::write00, this::read00);
        }

        private void write00(UpdateStreamEvent e, RevisionDataOutput target) throws IOException {
            target.writeUTF(e.scope);
            target.writeUTF(e.stream);
            target.writeLong(e.requestId);
        }

        private void read00(RevisionDataInput source, UpdateStreamEventBuilder b) throws IOException {
            b.scope(source.readUTF());
            b.stream(source.readUTF());
            b.requestId(source.readLong());
        }
    }

    //endregion
}

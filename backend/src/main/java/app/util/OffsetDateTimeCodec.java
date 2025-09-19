package app.util;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeCodec implements Codec<OffsetDateTime> {

    @Override
    public void encode(BsonWriter writer, OffsetDateTime value, org.bson.codecs.EncoderContext encoderContext) {
        writer.writeString(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    @Override
    public OffsetDateTime decode(BsonReader reader, org.bson.codecs.DecoderContext decoderContext) {
        return OffsetDateTime.parse(reader.readString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @Override
    public Class<OffsetDateTime> getEncoderClass() {
        return OffsetDateTime.class;
    }
}
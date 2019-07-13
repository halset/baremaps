package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.util.Accumulator;
import io.gazetteer.osm.util.StreamException;
import org.junit.jupiter.api.Test;

import java.util.Spliterator;
import java.util.stream.Collectors;

import static io.gazetteer.osm.OSMTestUtil.osmPbfData;
import static io.gazetteer.osm.OSMTestUtil.osmPbfInvalidBlock;
import static org.junit.jupiter.api.Assertions.*;

public class PBFUtilTest {

    @Test
    public void stream() {
        assertTrue(PbfUtil.stream(osmPbfData()).count() == 10);
    }

    @Test
    public void isHeaderBlock() {
        assertTrue(PbfUtil.stream(osmPbfData()).filter(PbfUtil::isHeaderBlock).count() == 1);
    }

    @Test
    public void isDataBlock() {
        assertTrue(PbfUtil.stream(osmPbfData()).filter(PbfUtil::isPrimitiveBlock).count() == 9);
    }

    @Test
    public void toHeaderBlock() {
        assertTrue(
                PbfUtil.stream(osmPbfData())
                        .filter(PbfUtil::isHeaderBlock)
                        .map(PbfUtil::toHeaderBlock)
                        .count()
                        == 1);
    }

    @Test
    public void toDataBlock() {
        assertTrue(
                PbfUtil.stream(osmPbfData())
                        .filter(PbfUtil::isPrimitiveBlock)
                        .map(PbfUtil::toPrimitiveBlock)
                        .collect(Collectors.toList())
                        .size()
                        == 9);
    }

    @Test
    public void toDataBlockException() {
        assertThrows(StreamException.class, () -> {
            PbfUtil.toPrimitiveBlock(osmPbfInvalidBlock());
        });
    }

    @Test
    public void tryAdvance() {
        Spliterator<FileBlock> spliterator = PbfUtil.spliterator(osmPbfData());
        for (int i = 0; i < 10; i++) {
            assertTrue(spliterator.tryAdvance(block -> {
            }));
        }
        assertFalse(spliterator.tryAdvance(block -> {
        }));
    }

    @Test
    public void forEachRemaining() {
        Spliterator<FileBlock> spliterator = PbfUtil.spliterator(osmPbfData());
        Accumulator<FileBlock> accumulator = new Accumulator<>();
        spliterator.forEachRemaining(accumulator);
        assertTrue(accumulator.acc.size() == 10);
    }
}

/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.io.ContentReference;

/**
 * Object that encapsulates Location information used for reporting
 * parsing (or potentially generation) errors, as well as current location
 * within input streams.
 */
public class JsonLocation
    implements java.io.Serializable
{
    private static final long serialVersionUID = 2L; // in 2.13

    /**
     * Shared immutable "N/A location" that can be returned to indicate
     * that no location information is available.
     */
    public final static JsonLocation NA = new JsonLocation(ContentReference.unknown(),
            -1L, -1L, -1, -1);

    private final static String NO_LOCATION_DESC = "[No location information]";

    protected final long _totalBytes;
    protected final long _totalChars;

    protected final int _lineNr;
    protected final int _columnNr;

    /**
     * Reference to input source; never null (but may be that of
     * {@link ContentReference#unknown()}).
     */
    protected final ContentReference _contentReference;

    /**
     * Lazily constructed description for source; constructed if and
     * when {@link #sourceDescription()} is called, retained.
     *
     * @since 2.13
     */
    protected transient String _sourceDescription;

    /*
    /**********************************************************************
    /* Life cycle
    /**********************************************************************
     */

    public JsonLocation(ContentReference contentRef, long totalChars,
            int lineNr, int colNr)
    {
        this(contentRef, -1L, totalChars, lineNr, colNr);
    }

    public JsonLocation(ContentReference contentRef, long totalBytes, long totalChars,
            int lineNr, int columnNr)
    {
        // 14-Mar-2021, tatu: Defensive programming, but also for convenience...
        if (contentRef == null) {
            contentRef = ContentReference.unknown();
        }
        _contentReference = contentRef;
        _totalBytes = totalBytes;
        _totalChars = totalChars;
        _lineNr = lineNr;
        _columnNr = columnNr;
    }

    /*
    /**********************************************************************
    /* Simple accessors
    /**********************************************************************
     */

    /**
     * Accessor for information about the original input source content is being
     * read from. Returned reference is never {@code null} but may not contain
     * useful information.
     *<p>
     * NOTE: not getter, on purpose, to avoid inlusion if serialized using
     * default Jackson serializer.
     *
     * @return Object with information about input source.
     */
    public ContentReference contentReference() {
        return _contentReference;
    }

    /**
     * Access for getting line number of this location, if available.
     * Note that line number is typically not available for binary formats.
     *
     * @return Line number of the location (1-based), if available; {@code -1} if not.
     */
    public int getLineNr() { return _lineNr; }

    /**
     * Access for getting column position of this location, if available.
     * Note that column position is typically not available for binary formats.
     *
     * @return Column position of the location (1-based), if available; {@code -1} if not.
     */
    public int getColumnNr() { return _columnNr; }

    /**
     * @return Character offset within underlying stream, reader or writer,
     *   if available; {@code -1} if not.
     */
    public long getCharOffset() { return _totalChars; }

    /**
     * @return Byte offset within underlying stream, reader or writer,
     *   if available; {@code -1} if not.
     */
    public long getByteOffset() { return _totalBytes; }

    /**
     * Accessor for getting a textual description of source reference
     * (Object returned by {@link #contentReference()}), as included in
     * description returned by {@link #toString()}.
     *<p>
     * Note: implementation will simply call
     * {@link ContentReference#buildSourceDescription()})
     *<p>
     * NOTE: not added as a "getter" to prevent it from getting serialized.
     *
     * @return Description of the source reference (see {@link #contentReference()}
     */
    public String sourceDescription() {
        // 04-Apr-2021, tatu: Construct lazily but retain
        if (_sourceDescription == null) {
            _sourceDescription = _contentReference.buildSourceDescription();
        }
        return _sourceDescription;
    }

    /**
     * Accessor for a brief summary of Location offsets (line number, column position,
     * or byte offset, if available).
     *
     * @return Description of available relevant location offsets; combination of
     *    line number and column position or byte offset
     */
    public String offsetDescription() {
        return appendOffsetDescription(new StringBuilder(40)).toString();
    }

    public StringBuilder appendOffsetDescription(StringBuilder sb) {
        sb.append("line: ");
        sb.append(_lineNr);
        sb.append(", column: ");
        sb.append(_columnNr);
        return sb;
    }

    /*
    /**********************************************************************
    /* Standard method overrides
    /**********************************************************************
     */

    @Override
    public int hashCode()
    {
        int hash = (_contentReference == null) ? 1 : 2;
        hash ^= _lineNr;
        hash += _columnNr;
        hash ^= (int) _totalChars;
        hash += (int) _totalBytes;
        return hash;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this) return true;
        if (other == null) return false;
        if (!(other instanceof JsonLocation)) return false;
        JsonLocation otherLoc = (JsonLocation) other;

        if (_contentReference == null) {
            if (otherLoc._contentReference != null) return false;
        } else if (!_contentReference.equals(otherLoc._contentReference)) {
            return false;
        }

        return (_lineNr == otherLoc._lineNr)
            && (_columnNr == otherLoc._columnNr)
            && (_totalChars == otherLoc._totalChars)
            && (_totalBytes == otherLoc._totalBytes)
            ;
    }

    @Override
    public String toString()
    {
        if (this == NA) {
            return NO_LOCATION_DESC;
        }
        final String srcDesc = sourceDescription();
        StringBuilder sb = new StringBuilder(40 + srcDesc.length())
                .append("[Source: ")
                .append(srcDesc)
                .append("; ");
        return appendOffsetDescription(sb)
                .append(']')
                .toString();
    }

    public StringBuilder toString(StringBuilder sb)
    {
        if (this == NA) {
            return sb.append(NO_LOCATION_DESC);
        }
        sb.append("[Source: ")
                .append(sourceDescription())
                .append("; ");
        return appendOffsetDescription(sb)
                .append(']');
    }
}

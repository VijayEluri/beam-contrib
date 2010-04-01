package org.esa.beam.dataio.envi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Map;
import java.util.Iterator;

class Header {

    static final String UNKNOWN_SENSOR_TYPE = "Unknown Sensor Type";
    static final String SENSING_START = "sensingStart";
    static final String SENSING_STOP = "sensingStop";
    static final String BEAM_PROPERTIES = "beamProperties";

    Header(final BufferedReader reader) throws IOException {
        // @todo 2 tb/tb exception handling - for ANY parse operation

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            line = line.trim();
            if (line.startsWith(EnviConstants.HEADER_KEY_SAMPLES)) {
                numSamples = Integer.parseInt(line.substring(line.indexOf('=') + 1).trim());
            } else if (line.startsWith(EnviConstants.HEADER_KEY_LINES)) {
                numLines = Integer.parseInt(line.substring(line.indexOf('=') + 1).trim());
            } else if (line.startsWith(EnviConstants.HEADER_KEY_BANDS)) {
                numBands = Integer.parseInt(line.substring(line.indexOf('=') + 1).trim());
            } else if (line.startsWith(EnviConstants.HEADER_KEY_HEADER_OFFSET)) {
                headerOffset = Integer.parseInt(line.substring(line.indexOf('=') + 1).trim());
            } else if (line.startsWith(EnviConstants.HEADER_KEY_FILE_TYPE)) {
                fileType = line.substring(line.indexOf('=') + 1).trim();
            } else if (line.startsWith(EnviConstants.HEADER_KEY_DATA_TYPE)) {
                dataType = Integer.parseInt(line.substring(line.indexOf('=') + 1).trim());
            } else if (line.startsWith(EnviConstants.HEADER_KEY_INTERLEAVE)) {
                interleave = line.substring(line.indexOf('=') + 1).trim();
            } else if (line.startsWith(EnviConstants.HEADER_KEY_SENSOR_TYPE)) {
                sensorType = line.substring(line.indexOf('=') + 1).trim();
            } else if (line.startsWith(EnviConstants.HEADER_KEY_BYTE_ORDER)) {
                byteOrder = Integer.parseInt(line.substring(line.indexOf('=') + 1).trim());
            } else if (line.startsWith(EnviConstants.HEADER_KEY_MAP_INFO)) {
                line = assembleMultilineString(reader, line);
                parseMapInfo(line);
            } else if (line.startsWith(EnviConstants.HEADER_KEY_PROJECTION_INFO)) {
                line = assembleMultilineString(reader, line);
                parseProjectionInfo(line);
            } else if (line.startsWith(EnviConstants.HEADER_KEY_BAND_NAMES)) {
                line = assembleMultilineString(reader, line);
                parseBandNames(line);
            } else if (line.startsWith(EnviConstants.HEADER_KEY_DESCRIPTION)) {
                line = assembleMultilineString(reader, line);
                description = line.substring(line.indexOf('{') + 1, line.lastIndexOf('}')).trim();
                try {
                    parseBeamProperties(description);
                } catch (ParseException e) {
                    //@todo handle this
                }
            }
        }
        // @todo 2 se/** after reading the headerFile validate the HeaderConstraints
    }

    ByteOrder getJavaByteOrder() {
        if (getByteOrder() == 1) {
            return ByteOrder.BIG_ENDIAN;
        } else {
            return ByteOrder.LITTLE_ENDIAN;
        }
    }

    String getFileType() {
        return fileType;
    }

    int getNumSamples() {
        return numSamples;
    }

    int getNumLines() {
        return numLines;
    }

    int getNumBands() {
        return numBands;
    }

    int getHeaderOffset() {
        return headerOffset;
    }

    int getDataType() {
        return dataType;
    }

    String getInterleave() {
        return interleave;
    }

    String getSensorType() {
        if (sensorType == null) {
            return UNKNOWN_SENSOR_TYPE;
        }
        return sensorType;
    }

    int getByteOrder() {
        return byteOrder;
    }

    EnviMapInfo getMapInfo() {
        return mapInfo;
    }

    EnviProjectionInfo getProjectionInfo() {
        return projectionInfo;
    }

    String[] getBandNames() {
        return bandNames;
    }

    String getDescription() {
        return description;
    }

    BeamProperties getBeamProperties() {
        return beamProperties;
    }
    ///////////////////////////////////////////////////////////////////////////
    /////// END OF PUBLIC
    ///////////////////////////////////////////////////////////////////////////

    private int numSamples = 0;
    private int numLines = 0;
    private int numBands = 0;
    private String fileType = null;
    private int headerOffset = 0;
    private int dataType = 0;
    private String interleave = null;
    private String sensorType = null;
    private int byteOrder = 0;
    private EnviMapInfo mapInfo = null;
    private EnviProjectionInfo projectionInfo = null;
    private String[] bandNames = null;
    private String description = null;
    private BeamProperties beamProperties = null;

    private void parseMapInfo(String line) {
        mapInfo = new EnviMapInfo();
        final StringTokenizer tokenizer = createTokenizerFromLine(line);
        mapInfo.setProjectionName(tokenizer.nextToken().trim());
        mapInfo.setReferencePixelX(Double.parseDouble(tokenizer.nextToken()));
        mapInfo.setReferencePixelY(Double.parseDouble(tokenizer.nextToken()));
        mapInfo.setEasting(Double.parseDouble(tokenizer.nextToken()));
        mapInfo.setNorthing(Double.parseDouble(tokenizer.nextToken()));
        mapInfo.setPixelSizeX(Double.parseDouble(tokenizer.nextToken()));
        mapInfo.setPixelSizeY(Double.parseDouble(tokenizer.nextToken()));
        mapInfo.setDatum(tokenizer.nextToken().trim());
        mapInfo.setUnit(tokenizer.nextToken().trim());
    }

    private static StringTokenizer createTokenizerFromLine(String line) {
        final int start = line.indexOf('{') + 1;
        final int stop = line.lastIndexOf('}');
        return new StringTokenizer(line.substring(start, stop), ",");
    }

    private void parseProjectionInfo(String line) {
        projectionInfo = new EnviProjectionInfo();
        final StringTokenizer tokenizer = createTokenizerFromLine(line);
        projectionInfo.setProjectionNumber(Integer.parseInt(tokenizer.nextToken().trim()));

        final ArrayList<Double> parameterList = new ArrayList<Double>(20);
        String token = null;
        try {
            while (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken().trim();
                parameterList.add(Double.parseDouble(token));
            }
        } catch (NumberFormatException e) {
            // ugly - but works. we encountered the first non-double token.
        }
        final double[] parameters = new double[parameterList.size()];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = parameterList.get(i);
        }
        projectionInfo.setParameter(parameters);

        projectionInfo.setDatum(token);
        projectionInfo.setName(tokenizer.nextToken().trim());
    }

    private static boolean withoutEndTag(String line) {
        return line.indexOf('}') < 0;
    }

    private static String assembleMultilineString(BufferedReader reader, String line) throws IOException {
        StringBuilder buffer = new StringBuilder(10);
        buffer.append(line);
        while (withoutEndTag(line)) {
            line = reader.readLine();
            buffer.append(line);
        }
        final String bufferString = buffer.toString();
        line = bufferString.replace('\n', ' ');
        return line;
    }

    private void parseBandNames(String line) {
        final StringTokenizer tokenizer = createTokenizerFromLine(line);
        bandNames = new String[tokenizer.countTokens()];
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            bandNames[index] = tokenizer.nextToken().trim();
            ++index;
        }
    }

    private void parseBeamProperties(final String txt) throws IOException, ParseException {
        if (txt.contains(BEAM_PROPERTIES)) {
            final int propsIdx = txt.indexOf(BEAM_PROPERTIES);
            final int openIdx = txt.indexOf('[', propsIdx);
            final int closeIdx = txt.indexOf(']', openIdx);
            final String beamProps = txt.substring(openIdx + 1, closeIdx);
            final String strings = beamProps.replace(',', '\n');
            final ByteArrayInputStream in = new ByteArrayInputStream(strings.getBytes());
            final Properties properties = loadProperties(in);
            final BeamProperties bean = new BeamProperties();
            if(properties.containsKey(Header.SENSING_START)) {
                 bean.setSensingStart(properties.getProperty(Header.SENSING_START));
            }
            if (properties.containsKey(Header.SENSING_STOP)) {
                bean.setSensingStop(properties.getProperty(Header.SENSING_STOP));
            }
            this.beamProperties = bean;
        }
    }

    public static Properties loadProperties(final InputStream in) throws IOException {
        final Properties properties = new Properties();
        try {
            properties.load(in);
        } finally {
            in.close();
        }
        return properties;
    }

    public static class BeamProperties {

        private String sensingStart = null;
        private String sensingStop = null;

        public String getSensingStart() {
            return sensingStart;
        }

        public String getSensingStop() {
            return sensingStop;
        }

        public void setSensingStart(String sensingStart) {
            this.sensingStart = sensingStart;
        }

        public void setSensingStop(String sensingStop) {
            this.sensingStop = sensingStop;
        }
    }
}

/**
 * Copyright (C) 2013, 2014, 2015 Johannes Taelman
 *
 * This file is part of Axoloti.
 *
 * Axoloti is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Axoloti is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Axoloti. If not, see <http://www.gnu.org/licenses/>.
 */
package generatedobjects;

import axoloti.attributedefinition.AxoAttributeComboBox;
import axoloti.attributedefinition.AxoAttributeTablename;
import axoloti.inlets.InletBool32Rising;
import axoloti.inlets.InletBool32RisingFalling;
import axoloti.inlets.InletCharPtr32;
import axoloti.inlets.InletFrac32;
import axoloti.inlets.InletFrac32Buffer;
import axoloti.object.AxoObject;
import axoloti.outlets.OutletBool32;
import axoloti.outlets.OutletFrac32Buffer;
import axoloti.outlets.OutletInt32Bipolar;
import axoloti.outlets.OutletInt32Pos;
import static generatedobjects.gentools.WriteAxoObject;

/**
 *
 * @author Johannes Taelman
 */
public class Wave extends gentools {

    static void GenerateAll() {
        String catName = "wave";
        WriteAxoObject(catName, Create_playWave2());
        WriteAxoObject(catName, Create_playWave2Stereo());
        WriteAxoObject(catName, Create_playWave2StereoPoly());
        WriteAxoObject(catName, Create_playWave3Mono());
        WriteAxoObject(catName, Create_playWave3Stereo());
        WriteAxoObject(catName, Create_playWave3Mono_fn());
        WriteAxoObject(catName, Create_playWave3Stereo_fn());
        WriteAxoObject(catName, Create_playFlashWave());
        WriteAxoObject(catName, Create_FlashWaveRead2());
        WriteAxoObject(catName, Create_Benchmark());
        //WriteAxoObject(dirname, Create_FlashWaveRead2());
        WriteAxoObject(catName, Create_recWave2());
        WriteAxoObject(catName, Create_Looper());
    }


    /*
     static AxoObject Create_playWave() {
     AxoObject o = new AxoObject("playwave", "streaming playback of a mono .wav file from sdcard (ok)");
     o.inlets.add(new InletFrac32("pos", "position"));
     o.inlets.add(new InletBool32RisingFalling("trig", "trigger"));
     o.outlets.add(new OutletFrac32Buffer("o", "output"));
     o.attributes.add(new AxoAttributeTablename("wavefile"));
     o.sLocalData = "void *stream;\n"
     + "   int ntrig;\n";
     o.sInitCode = "stream = sdOpenStream(\"%wavefile%\");\n"
     + "ntrig = 0;\n";
     o.sKRateCode = "    int16_t *p = sdReadStream(stream);\n"
     + "     int32_t i;\n"
     + "     if (p && ntrig)\n"
     + "        for(i=0;i<BUFSIZE;i++) %o%[i] = (*(p++))<<10;\n"
     + "     else \n"
     + "        for(i=0;i<BUFSIZE;i++) %o%[i] = 0;\n"
     + "     if ((%trig%>0) && !ntrig) {sdSeekStream(stream,((%pos%)>>4)<<2);  ntrig=1;}\n"
     + "     if (!(%trig%>0) && ntrig) ntrig=0;\n";
     return o;
     }
     */
    static AxoObject Create_playWave2() {
        AxoObject o = new AxoObject("play2", "streaming playback of a mono .wav file from sdcard (testing)");
        o.inlets.add(new InletFrac32("pos", "position"));
        o.inlets.add(new InletBool32RisingFalling("trig", "trigger"));
        o.inlets.add(new InletCharPtr32("filename", "file name"));
        o.outlets.add(new OutletFrac32Buffer("o", "output"));
        o.includes.add("chibios/ext/fatfs/src/ff.h");
        o.includes.add("./streamer.h");
        o.sLocalData = "    WORKING_AREA(waThreadSD, 512);\n"
                + "   sdReadFilePingpong *stream;\n"
                + "   int ntrig;\n";
        o.sInitCode = "static sdReadFilePingpong s __attribute__ ((section (\".rodata\")));\n"
                + "stream = &s;\n"
                + "stream->pingpong = CLOSED;\n"
                + "stream->doSeek = 0;\n"
                + "ntrig = 0;\n"
                + "stream->pThreadSD = chThdCreateStatic(waThreadSD, sizeof(waThreadSD),\n"
                + "                    HIGHPRIO, ThreadSD, (void *)stream);\n";
        o.sKRateCode = "     int32_t i;\n"
                + "     if ((%trig%>0) && !ntrig) {\n"
                + "        sdOpenStream(stream,%filename%);\n"
                + "        sdSeekStream(stream,((%pos%)>>4)<<2);\n"
                + "        ntrig=1;\n"
                + "     } else if ((!(%trig%>0)) && ntrig) {"
                + "        ntrig=0;\n"
                + "           sdCloseStream(stream);\n"
                + "     }\n"
                + "     int16_t *p = 0;\n"
                + "        p=sdReadStream(stream);\n"
                + "     if (p && ntrig)\n"
                + "        for(i=0;i<BUFSIZE;i++) %o%[i] = (*(p++))<<10;\n"
                + "     else \n"
                + "        for(i=0;i<BUFSIZE;i++) %o%[i] = 0;\n";
        o.sDisposeCode = "sdStopStreamer(stream);\n";
        return o;
    }

    static AxoObject Create_playWave2Stereo() {
        AxoObject o = new AxoObject("play2 stereo", "streaming playback of a stereo .wav file from sdcard (testing)");
        o.inlets.add(new InletFrac32("pos", "position"));
        o.inlets.add(new InletBool32RisingFalling("trig", "trigger"));
        o.inlets.add(new InletCharPtr32("filename", "file name"));
        o.outlets.add(new OutletFrac32Buffer("outl", "output left"));
        o.outlets.add(new OutletFrac32Buffer("outr", "output right"));
        o.includes.add("chibios/ext/fatfs/src/ff.h");
        o.includes.add("./streamer.h");
        o.sLocalData = "    WORKING_AREA(waThreadSD, 2048);\n"
                + "   sdReadFilePingpong *stream;\n"
                + "   int ntrig;\n";
        o.sInitCode = "static sdReadFilePingpong s __attribute__ ((section (\".rodata\")));\n"
                + "stream = &s;\n"
                + "stream->pingpong = CLOSED;\n"
                + "stream->doSeek = 0;\n"
                + "ntrig = 0;\n"
                + "stream->pThreadSD = chThdCreateStatic(waThreadSD, sizeof(waThreadSD),\n"
                + "                    HIGHPRIO, ThreadSD, (void *)stream);\n";
        o.sKRateCode = "     int32_t i;\n"
                + "     if ((%trig%>0) && !ntrig) {\n"
                + "        sdOpenStream(stream,%filename%);\n"
                + "        sdSeekStream(stream,((%pos%)>>4)<<2);\n"
                + "        ntrig=1;\n"
                + "     } else if ((!(%trig%>0)) && ntrig) {"
                + "        ntrig=0;\n"
                + "        sdCloseStream(stream);\n"
                + "     }\n"
                + "     int16_t *p = 0;\n"
                + "     int16_t *q = 0;\n"
                + "     if (%trig%>0) {"
                + "        p=sdReadStream(stream);\n"
                + "        q=sdReadStream(stream);\n"
                + "     }"
                + "     if (p && q && ntrig) {\n"
                + "        for(i=0;i<BUFSIZE/2;i++){\n"
                + "           %outl%[i] = (*(p++))<<10;\n"
                + "           %outr%[i] = (*(p++))<<10;\n"
                + "        }\n"
                + "        for(;i<BUFSIZE;i++){\n"
                + "           %outl%[i] = (*(q++))<<10;\n"
                + "           %outr%[i] = (*(q++))<<10;\n"
                + "        }\n"
                + "     }\n"
                + "     else \n"
                + "        for(i=0;i<BUFSIZE;i++) %outl%[i] = %outr%[i]= 0;\n";
        o.sDisposeCode = "sdStopStreamer(stream);\n";
        return o;
    }

    static AxoObject Create_playWave2StereoPoly() {
        AxoObject o = new AxoObject("play2 stereo poly", "streaming playback of a stereo .wav file from sdcard (testing), for polyphonic subpatches");
        o.inlets.add(new InletFrac32("pos", "position"));
        o.inlets.add(new InletBool32RisingFalling("trig", "trigger"));
        o.inlets.add(new InletCharPtr32("filename", "file name"));
        o.outlets.add(new OutletFrac32Buffer("outl", "output left"));
        o.outlets.add(new OutletFrac32Buffer("outr", "output right"));
        o.includes.add("chibios/ext/fatfs/src/ff.h");
        o.includes.add("./streamer.h");
        o.sLocalData = "    WORKING_AREA(waThreadSD, 1024);\n"
                + "   sdReadFilePingpong *stream;\n"
                + "   int ntrig;\n";
        o.sInitCode = "static sdReadFilePingpong s[%poly%] __attribute__ ((section (\".rodata\")));\n"
                + "stream = &s[parent2->polyIndex];\n"
                + "stream->pingpong = CLOSED;\n"
                + "stream->doSeek = 0;\n"
                + "ntrig = 0;\n"
                + "stream->pThreadSD = chThdCreateStatic(waThreadSD, sizeof(waThreadSD),\n"
                + "                    HIGHPRIO, ThreadSD, (void *)stream);\n";
        o.sKRateCode = "     int32_t i;\n"
                + "     if ((%trig%>0) && !ntrig) {\n"
                + "        sdOpenStream(stream,%filename%);\n"
                + "        sdSeekStream(stream,((%pos%)>>4)<<2);\n"
                + "        ntrig=1;\n"
                + "     } else if ((!(%trig%>0)) && ntrig) {"
                + "        ntrig=0;\n"
                + "        sdCloseStream(stream);\n"
                + "     }\n"
                + "     int16_t *p = 0;\n"
                + "     int16_t *q = 0;\n"
                + "     if (%trig%>0) {"
                + "        p=sdReadStream(stream);\n"
                + "        q=sdReadStream(stream);\n"
                + "     }"
                + "     if (p && q && ntrig) {\n"
                + "        for(i=0;i<BUFSIZE/2;i++){\n"
                + "           %outl%[i] = (*(p++))<<10;\n"
                + "           %outr%[i] = (*(p++))<<10;\n"
                + "        }\n"
                + "        for(;i<BUFSIZE;i++){\n"
                + "           %outl%[i] = (*(q++))<<10;\n"
                + "           %outr%[i] = (*(q++))<<10;\n"
                + "        }\n"
                + "     }\n"
                + "     else \n"
                + "        for(i=0;i<BUFSIZE;i++) %outl%[i] = %outr%[i]= 0;\n";
        o.sDisposeCode = "sdStopStreamer(stream);\n";
        return o;
    }

    static AxoObject Create_playWave3Mono() {
        AxoObject o = new AxoObject("play3", "streaming playback of a mono .wav file from sdcard (testing)");
        o.inlets.add(new InletFrac32("pos", "position"));
        o.inlets.add(new InletBool32Rising("start", "trigger"));
        o.inlets.add(new InletBool32Rising("stop", "trigger"));
        o.inlets.add(new InletCharPtr32("filename", "file name"));
        o.outlets.add(new OutletFrac32Buffer("out", "output"));
        o.includes.add("chibios/ext/fatfs/src/ff.h");
        o.includes.add("./streamer.h");
        o.sLocalData = "    WORKING_AREA(waThreadSD, 512);\n"
                + "   sdReadFilePingpong *stream;\n"
                + "   int starttrig;\n"
                + "   int stoptrig;\n";
        o.sInitCode = "static sdReadFilePingpong s __attribute__ ((section (\".rodata\")));\n"
                + "stream = &s;\n"
                + "stream->pingpong = CLOSED;\n"
                + "stream->doSeek = 0;\n"
                + "starttrig = 0;\n"
                + "stoptrig = 0;\n"
                + "stream->pThreadSD = chThdCreateStatic(waThreadSD, sizeof(waThreadSD),\n"
                + "                    HIGHPRIO, ThreadSD, (void *)stream);\n";
        o.sKRateCode = "     int32_t i;\n"
                + "     if ((%start%>0) && !starttrig) {\n"
                + "        sdOpenStream(stream,%filename%);\n"
                + "        sdSeekStream(stream,((%pos%)>>4)<<2);\n"
                + "        starttrig=1;\n"
                + "     } else if ((!(%start%>0)) && starttrig) {"
                + "        starttrig=0;\n"
                + "     }\n"
                + "     if ((%stop%>0) && !stoptrig) {\n"
                + "        sdCloseStream(stream);\n"
                + "        stoptrig=1;\n"
                + "     } else if ((!(%stop%>0)) && stoptrig) {"
                + "        stoptrig=0;\n"
                + "     }\n"
                + "     int16_t *p = 0;\n"
                + "     int16_t *q = 0;\n"
                + "     p=sdReadStream(stream);\n"
                + "     if (p) {\n"
                + "        for(i=0;i<BUFSIZE;i++){\n"
                + "           %out%[i] = (*(p++))<<10;\n"
                + "        }\n"
                + "     }\n"
                + "     else \n"
                + "        for(i=0;i<BUFSIZE;i++) %out%[i] = 0;\n";
        o.sDisposeCode = "sdStopStreamer(stream);\n";
        return o;
    }

    static AxoObject Create_playWave3Mono_fn() {
        AxoObject o = new AxoObject("play3 fn", "streaming playback of a mono .wav file from sdcard (testing). Direct filename.");
        o.inlets.add(new InletFrac32("pos", "position"));
        o.inlets.add(new InletBool32Rising("start", "trigger"));
        o.inlets.add(new InletBool32Rising("stop", "trigger"));
        o.attributes.add(new AxoAttributeTablename("fn"));
        o.outlets.add(new OutletFrac32Buffer("out", "output"));
        o.includes.add("chibios/ext/fatfs/src/ff.h");
        o.includes.add("./streamer.h");
        o.sLocalData = "    WORKING_AREA(waThreadSD, 512);\n"
                + "   sdReadFilePingpong *stream;\n"
                + "   int starttrig;\n"
                + "   int stoptrig;\n"
                + "   char c[64];\n";
        o.sInitCode = "static sdReadFilePingpong s __attribute__ ((section (\".rodata\")));\n"
                + "stream = &s;\n"
                + "stream->pingpong = CLOSED;\n"
                + "stream->doSeek = 0;\n"
                + "starttrig = 0;\n"
                + "stoptrig = 0;\n"
                + "strcpy(&c[0],\"%fn%\");\n"
                + "stream->pThreadSD = chThdCreateStatic(waThreadSD, sizeof(waThreadSD),\n"
                + "                    HIGHPRIO, ThreadSD, (void *)stream);\n";
        o.sKRateCode = "     int32_t i;\n"
                + "     if ((%start%>0) && !starttrig) {\n"
                + "        sdOpenStream(stream,&c[0]);\n"
                + "        sdSeekStream(stream,((%pos%)>>4)<<2);\n"
                + "        starttrig=1;\n"
                + "     } else if ((!(%start%>0)) && starttrig) {"
                + "        starttrig=0;\n"
                + "     }\n"
                + "     if ((%stop%>0) && !stoptrig) {\n"
                + "        sdCloseStream(stream);\n"
                + "        stoptrig=1;\n"
                + "     } else if ((!(%stop%>0)) && stoptrig) {"
                + "        stoptrig=0;\n"
                + "     }\n"
                + "     int16_t *p = 0;\n"
                + "     int16_t *q = 0;\n"
                + "     p=sdReadStream(stream);\n"
                + "     if (p) {\n"
                + "        for(i=0;i<BUFSIZE;i++){\n"
                + "           %out%[i] = (*(p++))<<10;\n"
                + "        }\n"
                + "     }\n"
                + "     else \n"
                + "        for(i=0;i<BUFSIZE;i++) %out%[i] = 0;\n";
        o.sDisposeCode = "sdStopStreamer(stream);\n";
        return o;
    }

    static AxoObject Create_playWave3Stereo() {
        AxoObject o = new AxoObject("play3 stereo", "streaming playback of a stereo .wav file from sdcard (testing)");
        o.inlets.add(new InletFrac32("pos", "position"));
        o.inlets.add(new InletBool32Rising("start", "trigger"));
        o.inlets.add(new InletBool32Rising("stop", "trigger"));
        o.inlets.add(new InletCharPtr32("filename", "file name"));
        o.outlets.add(new OutletFrac32Buffer("outl", "output left"));
        o.outlets.add(new OutletFrac32Buffer("outr", "output right"));
        o.includes.add("chibios/ext/fatfs/src/ff.h");
        o.includes.add("./streamer.h");
        o.sLocalData = "    WORKING_AREA(waThreadSD, 512);\n"
                + "   sdReadFilePingpong *stream;\n"
                + "   int starttrig;\n"
                + "   int stoptrig;\n";
        o.sInitCode = "static sdReadFilePingpong s __attribute__ ((section (\".rodata\")));\n"
                + "stream = &s;\n"
                + "stream->pingpong = CLOSED;\n"
                + "stream->doSeek = 0;\n"
                + "starttrig = 0;\n"
                + "stoptrig = 0;\n"
                + "stream->pThreadSD = chThdCreateStatic(waThreadSD, sizeof(waThreadSD),\n"
                + "                    HIGHPRIO, ThreadSD, (void *)stream);\n";
        o.sKRateCode = "     int32_t i;\n"
                + "     if ((%start%>0) && !starttrig) {\n"
                + "        sdOpenStream(stream,%filename%);\n"
                + "        sdSeekStream(stream,((%pos%)>>4)<<2);\n"
                + "        starttrig=1;\n"
                + "     } else if ((!(%start%>0)) && starttrig) {"
                + "        starttrig=0;\n"
                + "     }\n"
                + "     if ((%stop%>0) && !stoptrig) {\n"
                + "        sdCloseStream(stream);\n"
                + "        stoptrig=1;\n"
                + "     } else if ((!(%stop%>0)) && stoptrig) {"
                + "        stoptrig=0;\n"
                + "     }\n"
                + "     int16_t *p = 0;\n"
                + "     int16_t *q = 0;\n"
                + "     p=sdReadStream(stream);\n"
                + "     q=sdReadStream(stream);\n"
                + "     if (p && q) {\n"
                + "        for(i=0;i<BUFSIZE/2;i++){\n"
                + "           %outl%[i] = (*(p++))<<10;\n"
                + "           %outr%[i] = (*(p++))<<10;\n"
                + "        }\n"
                + "        for(;i<BUFSIZE;i++){\n"
                + "           %outl%[i] = (*(q++))<<10;\n"
                + "           %outr%[i] = (*(q++))<<10;\n"
                + "        }\n"
                + "     }\n"
                + "     else \n"
                + "        for(i=0;i<BUFSIZE;i++) %outl%[i] = %outr%[i]= 0;\n";
        o.sDisposeCode = "sdStopStreamer(stream);\n";
        return o;
    }

    static AxoObject Create_playWave3Stereo_fn() {
        AxoObject o = new AxoObject("play3 fn stereo", "streaming playback of a stereo .wav file from sdcard (testing)");
        o.inlets.add(new InletFrac32("pos", "position"));
        o.inlets.add(new InletBool32Rising("start", "trigger"));
        o.inlets.add(new InletBool32Rising("stop", "trigger"));
        o.attributes.add(new AxoAttributeTablename("fn"));
        o.outlets.add(new OutletFrac32Buffer("outl", "output left"));
        o.outlets.add(new OutletFrac32Buffer("outr", "output right"));
        o.includes.add("chibios/ext/fatfs/src/ff.h");
        o.includes.add("./streamer.h");
        o.sLocalData = "    WORKING_AREA(waThreadSD, 512);\n"
                + "   sdReadFilePingpong *stream;\n"
                + "   int starttrig;\n"
                + "   int stoptrig;\n"
                + "   char c[64];\n";
        o.sInitCode = "static sdReadFilePingpong s __attribute__ ((section (\".rodata\")));\n"
                + "stream = &s;\n"
                + "stream->pingpong = CLOSED;\n"
                + "stream->doSeek = 0;\n"
                + "starttrig = 0;\n"
                + "stoptrig = 0;\n"
                + "strcpy(&c[0],\"%fn%\");\n"
                + "stream->pThreadSD = chThdCreateStatic(waThreadSD, sizeof(waThreadSD),\n"
                + "                    HIGHPRIO, ThreadSD, (void *)stream);\n";
        o.sKRateCode = "     int32_t i;\n"
                + "     if ((%start%>0) && !starttrig) {\n"
                + "        sdOpenStream(stream,&c[0]);\n"
                + "        sdSeekStream(stream,((%pos%)>>4)<<2);\n"
                + "        starttrig=1;\n"
                + "     } else if ((!(%start%>0)) && starttrig) {"
                + "        starttrig=0;\n"
                + "     }\n"
                + "     if ((%stop%>0) && !stoptrig) {\n"
                + "        sdCloseStream(stream);\n"
                + "        stoptrig=1;\n"
                + "     } else if ((!(%stop%>0)) && stoptrig) {"
                + "        stoptrig=0;\n"
                + "     }\n"
                + "     int16_t *p = 0;\n"
                + "     int16_t *q = 0;\n"
                + "     p=sdReadStream(stream);\n"
                + "     q=sdReadStream(stream);\n"
                + "     if (p && q) {\n"
                + "        for(i=0;i<BUFSIZE/2;i++){\n"
                + "           %outl%[i] = (*(p++))<<10;\n"
                + "           %outr%[i] = (*(p++))<<10;\n"
                + "        }\n"
                + "        for(;i<BUFSIZE;i++){\n"
                + "           %outl%[i] = (*(q++))<<10;\n"
                + "           %outr%[i] = (*(q++))<<10;\n"
                + "        }\n"
                + "     }\n"
                + "     else \n"
                + "        for(i=0;i<BUFSIZE;i++) %outl%[i] = %outr%[i]= 0;\n";
        o.sDisposeCode = "sdStopStreamer(stream);\n";
        return o;
    }

    static AxoObject Create_playFlashWave() {
        AxoObject o = new AxoObject("flashplay", "Single-shot playback of a sample table in flash, without transposition");
        o.inlets.add(new InletBool32Rising("trig", "trigger"));
        o.outlets.add(new OutletFrac32Buffer("out", "wave"));
        String mentries[] = {
            "808bd",
            "808hatclose",
            "808hatopen",
            "808hitom",
            "808midtom",
            "808lotom",
            "808snare"
        };
        o.attributes.add(new AxoAttributeComboBox("sample", mentries, mentries));
        o.sLocalData = "uint32_t _pos;\n"
                + "int ntrig;\n";

        o.sInitCode = "extern int16_t _binary_%sample%_raw_size;\n"
                + "_pos=(int32_t)&_binary_%sample%_raw_size;\n";
        o.sKRateCode = "extern int16_t _binary_%sample%_raw_size;\n"
                + "extern int16_t _binary_%sample%_raw_start;\n"
                + "if ((%trig%>0) && !ntrig) {_pos=0;  ntrig=1;}\n"
                + "if (!(%trig%>0)) ntrig=0;\n"
                + "     int32_t i;\n"
                + "if (_pos<((int)(&_binary_%sample%_raw_size)/2)-BUFSIZE) {\n"
                + "    for(i=0;i<BUFSIZE;i++) (%out%)[i] = (((int16_t *)(&_binary_%sample%_raw_start))[_pos++])<<12;\n"
                + "} else {\n"
                + "    for(i=0;i<BUFSIZE;i++) (%out%)[i] = 10000;\n"
                + "}\n";
        return o;
    }

    static AxoObject Create_recWave2() {
        AxoObject o = new AxoObject("record", "streaming recording of a mono RAW audio file from sdcard (BROKEN: clicks)");
        o.inlets.add(new InletBool32Rising("open", "open file for recording"));
        o.inlets.add(new InletBool32Rising("start", "start recording"));
        o.inlets.add(new InletBool32Rising("stop", "stop recording and close file"));
//        o.inlets.add(new InletCharPtr32("filename", "file name"));
        o.attributes.add(new AxoAttributeTablename("fn"));
        o.inlets.add(new InletFrac32Buffer("in", "input"));
        o.outlets.add(new OutletInt32Bipolar("state", "recording state"));
        o.includes.add("chibios/ext/fatfs/src/ff.h");
        o.includes.add("./streamer.h");
        o.sLocalData = "    WORKING_AREA(waThreadSD, 1024);\n"
                + "   sdReadFilePingpong *stream;\n"
                + "   int open_prev;\n"
                + "   int start_prev;\n"
                + "   int stop_prev;\n"
                + "   int recording_active;\n"
                + "   char c[64];\n";
        o.sInitCode = "static sdReadFilePingpong s __attribute__ ((section (\".sram2\")));\n"
                + "stream = &s;\n"
                + "stream->pingpong = CLOSED;\n"
                + "stream->doSeek = 0;\n"
                + "open_prev = 0;\n"
                + "start_prev = 0;\n"
                + "stop_prev = 0;\n"
                + "recording_active = 0;\n"
                + "strcpy(&c[0],\"%fn%\");\n"
                + "stream->pThreadSD = chThdCreateStatic(waThreadSD, sizeof(waThreadSD),\n"
                + "                    HIGHPRIO, ThreadSD, (void *)stream);\n";
        o.sKRateCode = "     int32_t i;\n"
                + "     if ((%open%>0) && !open_prev) {\n"
                + "        recording_active = 0;\n"
                + "        sdOpenStreamRec(stream,&c[0]);\n"
                + "     } else if ((%stop%>0) && !stop_prev) {\n"
                + "        recording_active = 0;\n"
                + "        if (stream) \n"
                + "           sdCloseStreamRec(stream);\n"
                + "        stream = 0;\n"
                + "     } else if ((%start%>0) && !start_prev) {\n"
                + "        if (stream)\n"
                + "           recording_active = 1;\n"
                + "     }\n"
                + "     int16_t *p = 0;\n"
                + "     if (recording_active && stream) {\n"
                + "        p=sdWriteStream(stream);\n"
                + "        if (p)\n"
                + "           for(i=0;i<BUFSIZE;i++) (*(p++)) = %in%[i]>>12;\n"
                + "     }\n"
                + "open_prev = %open%;\n"
                + "start_prev = %start%;\n"
                + "stop_prev = %stop%;\n"
                + "if (p)"
                + "   %state% = stream->pingpong;\n"
                + "else %state% = -1;";
        o.sDisposeCode = "sdStopStreamer(stream);\n";
        return o;
    }

    static AxoObject Create_Looper() {
        AxoObject o = new AxoObject("longdelay", "streaming echo via sdcard");
        o.attributes.add(new AxoAttributeTablename("fn"));
        o.inlets.add(new InletFrac32Buffer("in", "input"));
        o.inlets.add(new InletFrac32("delay", "delay time"));
        o.outlets.add(new OutletFrac32Buffer("out", "delay out"));
        o.outlets.add(new OutletInt32Bipolar("state", "recording state"));
        o.outlets.add(new OutletInt32Bipolar("state2", "playback state"));
        o.includes.add("chibios/ext/fatfs/src/ff.h");
        o.includes.add("./looper.h");
        o.sLocalData = "   WORKING_AREA(waThreadSD, 1024);\n"
                + "   sdFilePingpongRW *stream;\n"
                + "   char c[64];\n";
        o.sInitCode = "static sdFilePingpongRW _stream __attribute__ ((section (\".data\")));\n"
                + "stream = &_stream;\n"
                + "stream->playpong = CLOSED;\n"
                + "stream->recpong = CLOSED;\n"
                + "\n"
                + "strcpy(&stream->filename[0],\"%fn%\");\n"
                + "stream_open(stream);\n"
                + "//stream->rpos = 0;\n"
                + "stream->wpos = 1024*48*2;\n"
                + "stream->pThreadSD = chThdCreateStatic(waThreadSD, sizeof(waThreadSD),\n"
                + "                    HIGHPRIO, ThreadSD, (void *)stream);\n";
        o.sKRateCode = "int16_t *p = 0;\n"
                + "int i;\n"
                + "\n"
                + "stream->delay = %delay%>>8;\n"
                + "\n"
                + "p=sdWriteStream(stream);\n"
                + "if (p)\n"
                + "   for(i=0;i<BUFSIZE;i++)\n"
                + "      (*(p++)) = %in%[i]>>12;\n"
                + "\n"
                + "//%state% = stream->playpong;\n"
                + "%state% = (int32_t)stream->wpos;\n"
                + "\n"
                + "p=sdReadStream(stream);\n"
                + "if (p) \n"
                + "   for(i=0;i<BUFSIZE;i++)\n"
                + "	%out%[i] = (*(p++))<<12;\n"
                + "\n"
                + "\n"
                + "//%state2% = stream->recpong;\n"
                + "//%state2% = (int32_t)p;\n"
                + "%state2% = (int32_t)stream->delay;";
        o.sDisposeCode = "stream_close(stream);\n";
        return o;
    }

    static AxoObject Create_FlashWaveRead2() {
        AxoObject o = new AxoObject("flashread2~~", "linear interpolated flash table read");
        o.inlets.add(new InletFrac32Buffer("pos", "position"));
        o.outlets.add(new OutletFrac32Buffer("o", "output"));
        {
            String mentries[] = {
                "808bd",
                "808hatclose",
                "808hatopen",
                "808hitom",
                "808midtom",
                "808lotom",
                "808snare",
                "rockwehrmann"
            };
            o.attributes.add(new AxoAttributeComboBox("sample", mentries, mentries));
        }
        {
            String mentries[] = {"2", "4", "8", "16", "32", "64", "128", "256", "512",
                "1024", "2048", "4096", "8192", "16384", "32768", "65536"};
            String centries[] = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"};
            o.attributes.add(new AxoAttributeComboBox("size", mentries, centries));
        }
        o.sLocalData = "static const uint32_t LENGTHPOW = (%size%);\n"
                + "static const uint32_t LENGTH = (1<<%size%);\n"
                + "static const uint32_t LENGTHMASK = ((1<<%size%)-1);\n"
                + "static const uint32_t BITS = 16;\n"
                + "static const uint32_t GAIN = 12;\n";
        o.sSRateCode = "extern int16_t _binary_%sample%_raw_start;\n"
                + "   uint32_t asat = __USAT(%pos%,27);\n"
                + "    int index = asat>>(27-LENGTHPOW);\n"
                + "   int32_t y1 = (((int16_t *)(&_binary_%sample%_raw_start))[index])<<GAIN;\n"
                + "   int32_t y2 = (((int16_t *)(&_binary_%sample%_raw_start))[index+1])<<GAIN;\n"
                + "   int frac = (asat - (index<<(27-LENGTHPOW)))<<(LENGTHPOW+3);\n"
                + "  int32_t rr;\n"
                + "  rr = ___SMMUL(y1,(1<<30)-frac);\n"
                + "  rr = ___SMMLA(y2,frac,rr);\n"
                + "%o%= rr<<2;\n";
        return o;
    }

    static AxoObject Create_Benchmark() {
        AxoObject o = new AxoObject("sdbenchmark", "sdcard benchmark");
        o.includes.add("./sdbenchmark.h");
        o.sInitCode = "sdbenchmark();\n";
        return o;
    }
}

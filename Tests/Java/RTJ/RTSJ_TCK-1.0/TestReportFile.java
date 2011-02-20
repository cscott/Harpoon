/* TestReportFile - Output report file class the RTSJ API tests
*/

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

import java.io.*;

public class TestReportFile extends Object {

    private static File f;
    private static PrintStream rpt;

    public  TestReportFile() {
        System.out.println("TestReportFile: TestReportFile()");
        try {
            f = new File("/tmp/CallTestsReport");

            f.createNewFile();
            rpt = new PrintStream( new FileOutputStream(f));
        }
        catch( IOException e ) {
            System.out.println("Failure to create CallTestsReportFile");
        }
    }

    public void println(String s) {
        System.out.println("TestReportFile: Writing '"+s+"'");
        rpt.println(s);

        System.out.flush();
        rpt.flush();
    }

}

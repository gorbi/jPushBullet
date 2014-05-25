package com.gorbi;

public class Push {
    public static void main(String args[]) {
        try {
            PushBullet pushBullet = new PushBullet();
            pushBullet.loadConfig(args[0]);
            if (args[1].equalsIgnoreCase("note")) {
                pushBullet.pushNote(args[2],args[3]);
            } else if(args[1].equalsIgnoreCase("link")) {
                if(args.length==4)
                    pushBullet.pushLink(args[2],args[3]);
                else
                    pushBullet.pushLink(args[2],args[3],args[4]);
            } else if (args[1].equalsIgnoreCase("address")) {
                pushBullet.pushAddress(args[2], args[3]);
            } else if (args[1].equalsIgnoreCase("list")) {
                String items[] = new String[args.length-3];
                for (int i = 3; i < args.length; i++) {
                    items[i-3]=args[i];
                }
                pushBullet.pushList(args[2],items);
            }

        } catch (ArrayIndexOutOfBoundsException ae) {
            //Do nothing
            System.out.println("To send note, params: path/To/Config/File note title body");
            System.out.println("To send link, params: path/To/Config/File link title url body(optional)");
            System.out.println("To send address, params: path/To/Config/File address name address");
            System.out.println("To send list, params: path/To/Config/File list title list....");
        } catch (Exception ae) {
            //Do nothing
        }
    }
}

package webcamcaptureplugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.capture.RecordableConfiguration;
import mo.organization.Configuration;
import mo.organization.Participant;
import mo.organization.ProjectOrganization;

public class WebcamCaptureConfiguration implements RecordableConfiguration {
    
    private String id;
    private int id_camera;
    private int fps;
    private int dim;
    WebcamRecorder wr;  

    WebcamCaptureConfiguration(String id,int id_camera, int fps,int dim) {
        this.id = id;
        this.id_camera = id_camera;
        this.fps = fps;
        this.dim = dim;
    }
    
    WebcamCaptureConfiguration(){
        
    }

    @Override
    public void setupRecording(File stageFolder, ProjectOrganization org, Participant p) {
         wr = new WebcamRecorder(stageFolder, org, p,id_camera,fps,dim, this);
    }

    @Override
    public void startRecording() {
            wr.StartRecord();
    }

    @Override
    public void stopRecording() {
        wr.StopRecord();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public File toFile(File parent) {
        try {
            File f = new File(parent, "webcam_"+id+"-"+id_camera+"_"+fps+"-"+dim+".xml");
            f.createNewFile();
            return f;
        } catch (IOException ex) {
            Logger.getLogger(WebcamCaptureConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Configuration fromFile(File file) {
        String fileName = file.getName();
        if (fileName.contains("_") && fileName.contains(".") && fileName.contains("-")){
            String newId = fileName.substring(fileName.indexOf('_') + 1, fileName.indexOf("-"));
            String newId_camera = fileName.substring(fileName.indexOf('-') + 1, fileName.lastIndexOf("_"));
            String newfps = fileName.substring(fileName.lastIndexOf('_') + 1, fileName.lastIndexOf("-"));
            String newdim = fileName.substring(fileName.lastIndexOf('-') + 1, fileName.lastIndexOf("."));
            WebcamCaptureConfiguration c = new WebcamCaptureConfiguration(newId,Integer.parseInt(newId_camera),Integer.parseInt(newfps),Integer.parseInt(newdim));
            return c;
        }
        return null;
    }

    @Override
    public void cancelRecording() {
        wr.CandelRecord();
    }

    @Override
    public void pauseRecording() {
        wr.PauseRecord();
    }

    @Override
    public void resumeRecording() {
        wr.ResumeRecod();
    }
    
}

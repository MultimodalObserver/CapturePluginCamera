package webcamcaptureplugin;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryService;
import com.github.sarxos.webcam.WebcamResolution;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.organization.FileDescription;
import mo.organization.Participant;
import mo.organization.ProjectOrganization;



public class WebcamRecorder {
    
    Participant participant;
    ProjectOrganization org;
    private WebcamCaptureConfiguration config;
    private File output;
    private File arInicio;
    private File frames;
    private String path;
    private String file_name;
    private FileOutputStream outputStream;    
    private FileDescription desc;    
    private long start;
    private long pause;
    private long inicio=0;
    private long fin=0;
    private static WebcamDiscoveryService discovery = Webcam.getDiscoveryService(); 
    
    private static final Logger logger = Logger.getLogger(WebcamRecorder.class.getName());
    
    public int id_camera;
    //public int fps_op;
    public int sleep;
    public long fps_aux;
    public int sw=1;
    public int IMG_WIDTH;
    public int IMG_HEIGHT;
    public long time;
    
    public WebcamRecorder(File stageFolder, ProjectOrganization org, Participant p,int ID,int FPS,int DIM,WebcamCaptureConfiguration c){
        participant = p;
        this.org = org;
        this.config = c;
        this.id_camera=ID;
        //this.fps_op=FPS;
        this.sleep = (int)(1000/FPS);
        createFile(stageFolder);
        switch(DIM){
            case 0:                
                IMG_WIDTH = 176;
                IMG_HEIGHT = 144;
                break;
            case 1:
                IMG_WIDTH = 320;
                IMG_HEIGHT = 240;
                break;
            case 2:                
                IMG_WIDTH = 640;
                IMG_HEIGHT = 480;
                break;
            case 3:
                IMG_WIDTH = 800;
                IMG_HEIGHT = 600;
                break;
            case 4:
                IMG_WIDTH = 1024;
                IMG_HEIGHT = 768;
                break;
            case 5:
                IMG_WIDTH = 1280;
                IMG_HEIGHT = 720;
                break;
            case 6:
                IMG_WIDTH = 1366;
                IMG_HEIGHT = 768;
                break;
            case 7:
                IMG_WIDTH = 1920;
                IMG_HEIGHT = 1080;
                break;
        }
    }

    private void createFile(File parent) {

        Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");

        String reportDate = df.format(now);

        output = new File(parent, reportDate + "_" + config.getId() + ".mp4");
        arInicio = new File(parent, reportDate + "_" + config.getId() + "-temp.txt");
        //frames = new File(parent, reportDate + "_" + config.getId() + "-frames.txt");
        path = parent.getAbsolutePath();
        file_name = reportDate + "_"+config.getId();
        try {
            output.createNewFile();
            outputStream = new FileOutputStream(output);
            desc = new FileDescription(output, WebcamRecorder.class.getName());
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        

    }
    
    private void deleteFile() {
        if (output.isFile()) {
            output.delete();
        }
        if (desc.getDescriptionFile().isFile()) {
            desc.deleteFileDescription();
        }
    }

	private class Record implements Runnable{
            @Override
            public void run() {

                List<Webcam> wCam = Webcam.getWebcams();
		IMediaWriter writer = ToolFactory.makeWriter(path+"\\"+file_name+".mp4");
                Dimension size = new Dimension(IMG_WIDTH,IMG_HEIGHT);
                /*if(IMG_WIDTH>640){                    
                    size = WebcamResolution.VGA.getSize();
                }
                else{                    
                    size = new Dimension(IMG_WIDTH,IMG_HEIGHT);
                }*/
                Dimension[] nonStandardResolutions = new Dimension[] {
			new Dimension(1024,768),
			new Dimension(800,600),
                        new Dimension(1280,720),
                        new Dimension(176,144),
                        new Dimension(320,240),
                        new Dimension(640,480),
                        new Dimension(1366,768),
                        new Dimension(1920,1080),
		};
		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_H264, IMG_WIDTH, IMG_HEIGHT);
		Webcam webcam = wCam.get(id_camera);
		webcam.setCustomViewSizes(nonStandardResolutions);
                discovery.stop();
		webcam.setViewSize(size);
		webcam.open(true); 
                int i= 0;
                int init =0;
                //ArrayList<Long> frames_n = new ArrayList();
		while(sw!=0) {                        
			BufferedImage image = ConverterFactory.convertToType(webcam.getImage(), BufferedImage.TYPE_3BYTE_BGR);
                        int type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
                        //if(size.width!=IMG_WIDTH && size.height !=IMG_HEIGHT){ image = resizeImage(image,type);}
                        image = ConverterFactory.convertToType(image, BufferedImage.TYPE_3BYTE_BGR);
                        time = System.currentTimeMillis();
			IConverter converter = ConverterFactory.createConverter(image, IPixelFormat.Type.YUV420P);
                        if(init==0){
                            inicio = time;
                            start = time;
                            fps_aux = time+sleep;
                            init=1;
                        }
			IVideoPicture frame = converter.toPicture(image, (time-start) * 1000);
			frame.setKeyFrame(i == 0);
			frame.setQuality(0);
                        //frames_n.add(time);
			writer.encodeVideo(0, frame);
                        i++;
                        while(fps_aux>System.currentTimeMillis()){
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(WebcamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        fps_aux=fps_aux+sleep;
                        /*switch(fps_op){
                                case 0:
                            {
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(WebcamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                                case 1:
                            {
                                try {
                                    Thread.sleep(25);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(WebcamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                                case 2:
                            {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(WebcamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                                case 3:
                            {
                                try {
                                    Thread.sleep(4);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(WebcamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                                case 4: 
                            }*/
                        while(sw==2){                           
                            try {       
                                Thread.sleep(2);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(WebcamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
		}
                /*BufferedWriter bw2;
                try {
                    bw2 = new BufferedWriter(new FileWriter(frames));
                for(int j=0;j<frames_n.size();j++){                    
                        bw2.write(frames_n.get(j)+"\n");
                }
                bw2.close();
                } catch (IOException ex) {                
                    Logger.getLogger(WebcamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                }*/
                fin = time;
		writer.close();
                webcam.close();
                BufferedWriter bw;
                try {
                    bw = new BufferedWriter(new FileWriter(arInicio));
                    bw.write(inicio+"\n");
                    bw.write(fin+"");
                    bw.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                try {
                    arInicio.createNewFile();
                } catch (IOException ex) {
                   logger.log(Level.SEVERE, null, ex);
                }
            }
	}
        
        public void StartRecord(){            
                Thread t=new Thread(new Record());
                t.start();  
        }
        
        public void StopRecord(){
            sw=0;
        }
        
        public void PauseRecord(){
             sw=2;
            pause = System.currentTimeMillis()-start;
        }
        
        public void ResumeRecod(){
            sw=1;
            start=System.currentTimeMillis()-pause+1000;
        }
        public void CandelRecord(){
            StopRecord();
            deleteFile();
        }
}
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
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    private String path;
    private String file_name;
    private FileOutputStream outputStream;    
    private FileDescription desc;    
    static long start;
    static long pause;
    private static long inicio;
    private static long fin;
    private static WebcamDiscoveryService discovery = Webcam.getDiscoveryService(); 
    
    private static final Logger logger = Logger.getLogger(WebcamRecorder.class.getName());
    
    public int id_camera;
    public int fps_op;
    public int sw=1;
    
    public WebcamRecorder(File stageFolder, ProjectOrganization org, Participant p,int ID,int FPS,WebcamCaptureConfiguration c){
        participant = p;
        this.org = org;
        this.config = c;
        this.id_camera=ID;
        this.fps_op=FPS;
        createFile(stageFolder);
    }

    private void createFile(File parent) {

        Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");

        String reportDate = df.format(now);

        output = new File(parent, reportDate + "_" + config.getId() + ".mp4");
        arInicio = new File(parent, reportDate + "_" + config.getId() + "-temp.txt");
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
		Dimension size = WebcamResolution.VGA.getSize();

		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_H264, size.width, size.height);

		Webcam webcam = wCam.get(id_camera);
                discovery.stop();
		webcam.setViewSize(size);
		webcam.open(true);
                start = System.currentTimeMillis();
                inicio = System.currentTimeMillis();                
                int i= 0;
		while(sw!=0) {

			BufferedImage image = ConverterFactory.convertToType(webcam.getImage(), BufferedImage.TYPE_3BYTE_BGR);
			IConverter converter = ConverterFactory.createConverter(image, IPixelFormat.Type.YUV420P);

			IVideoPicture frame = converter.toPicture(image, (System.currentTimeMillis() - start) * 1000);
			frame.setKeyFrame(i == 0);
			frame.setQuality(0);

			writer.encodeVideo(0, frame);
                        i++;
                        switch(fps_op){
                                case 0:
                            {
                                try {
                                    Thread.sleep(25);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(WebcamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                                case 1:
                            {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(WebcamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                                case 2:
                            {
                                try {
                                    Thread.sleep(4);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(WebcamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                                case 3: 
                            }
                        while(sw==2){                           
                            try {       
                                Thread.sleep(2);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(WebcamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
		}
                fin = System.currentTimeMillis();
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
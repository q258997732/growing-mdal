package bob.growingmdal.service;

import bob.growingmdal.util.reader.DekaReader;
import org.springframework.stereotype.Service;

@Service
public class DekaService {

    DekaReader dekaReader = DekaReader.load();

    public int initDevice(){
        return dekaReader.dc_init(DekaReader.PORT_USB, DekaReader.BAUD);
    }

    public Object getIDCardInfo(){

        return null;


    }


}

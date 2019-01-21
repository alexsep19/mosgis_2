package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import ru.eludia.products.mosgis.rest.api.base.CRUDBackend;
import ru.eludia.products.mosgis.rest.api.base.FileBackend;

@Local
public interface InXlFilesLocal extends CRUDBackend, FileBackend {

}
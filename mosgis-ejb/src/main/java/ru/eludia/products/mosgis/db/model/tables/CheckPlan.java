package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class CheckPlan extends EnTable {

    public enum c implements EnColEnum {
        
        YEAR                        (Type.NUMERIC, 4, "Год плана"),
        SIGN                        (Type.BOOLEAN, "Признак подписания"),
        SHOULDNOTBEREGISTERED       (Type.BOOLEAN, "Не должен быть зарегестрирован в ЕРП"),
        
        SHOULDBEREGISTERED          (Type.BOOLEAN,     new Virt ("DECODE(\"SHOULDNOTBEREGISTERED\",1,0,1)"), "Должен быть зарегестрирован в ЕРП"),
        URIREGISTRATIONPLANNUMBER   (Type.NUMERIC, 12, null, "Регистрационный номер плана в ЕРП"),
        
        INSPECTIONPLANGUID          (Type.UUID,        null, "Идентификатор плана проверок в ГИС ЖКХ"),
        REGISTRYNUMBER              (Type.STRING, 255, null, "Реестровый номер плана проверок"),
        
        ID_STATUS                   (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус"),
        UUID_ORG                    (VocOrganization.class,  "Организация, создавшая план"),
        GIS_UPDATE_DATE             (Type.DATETIME,   null,  "Дата изменения в ГИС ЖКХ"),
        
        ID_LOG                      (CheckPlanLog.class, "Последнее событие редактирования")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class<?> c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }
        
    }
    
    public CheckPlan () {
        
        super ("tb_check_plans", "Планы проверок");
        
        cols (c.class);
        
    }
    
	public enum Action {

		SEND_TO_GIS(VocGisStatus.i.PENDING_RP_PLACING, VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING);

		VocGisStatus.i nextStatus;
		VocGisStatus.i okStatus;
		VocGisStatus.i failStatus;

		private Action(VocGisStatus.i nextStatus, VocGisStatus.i okStatus, VocGisStatus.i failStatus) {
			this.nextStatus = nextStatus;
			this.okStatus = okStatus;
			this.failStatus = failStatus;
		}

		public VocGisStatus.i getNextStatus() {
			return nextStatus;
		}

		public VocGisStatus.i getFailStatus() {
			return failStatus;
		}

		public VocGisStatus.i getOkStatus() {
			return okStatus;
		}

		public static Action forStatus(VocGisStatus.i status) {
			switch (status) {
				case PENDING_RQ_PLACING: return SEND_TO_GIS;
				case PENDING_RP_PLACING: return SEND_TO_GIS;
				default: return null;
			}
		}

	}
	
	public enum Objects {
		INSPECTION_PLAN     (CheckPlan.class,          "План проверок",     CheckPlan.c.INSPECTIONPLANGUID.lc()), 
		PLANNED_EXAMINATION (PlannedExamination.class, "Плановая проверка", PlannedExamination.c.PLANNEDEXAMINATIONGUID.lc());

		private Class<?> clazz;
		private String name;
		private String gisKey;

		private Objects(Class<?> clazz, String name, String gisKey) {
			this.clazz = clazz;
			this.name = name;
			this.gisKey = gisKey;
		}

		public Class<?> getClazz() {
			return clazz;
		}

		public String getName() {
			return name;
		}

		public String getGisKey() {
			return gisKey;
		}
	}
    
}

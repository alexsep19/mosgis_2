package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPlannedExaminationStatus;

public class PlannedExamination extends EnTable {
    
    public enum c implements EnColEnum {
        
        CHECK_PLAN_UUID                 (CheckPlan.class, "План проверки"),
        
        NUMBERINPLAN                    (Type.NUMERIC, 3, "Номер проверки в плане"),
        URIREGISTRATIONNUMBER           (Type.NUMERIC, 14, null, "Номер проверки в ЕРП"),
        URIREGISTRATIONDATE             (Type.DATE, null, "Дата присвоения номера в ЕРП"),
        
        // --- RegulatoryAuthorityInformation ---
        REGULATOR_UUID                  (VocOrganization.class, "Орган жилищного надзора (контроля)"),
        FUNCTIONREGISTRYNUMBER          (Type.NUMERIC, 36, null, "Реестровый номер функции органа жилищного надзора в системе «Федеральный реестр государственных и муниципальных услуг»"),
        AUTHORIZEDPERSONS               (Type.STRING, 2000, null, "ФИО и должность лиц, уполномоченных на проведение проверки"),
        INVOLVEDEXPERTS                 (Type.STRING, 2000, null, "ФИО и должность экспертов, привлекаемых к проведению проверки"),
        
        // --- Subject ---
        SUBJECT_UUID                    (VocOrganization.class, "Субъект проверки"),
        ACTUALACTIVITYPLACE             (Type.STRING, 4000, null, "Место фактического осуществления деятельности субъекта проверки"),
        SMALLBUSINESS                   (Type.BOOLEAN, null, "Субъект малого предпринимательства"),
        
        CODE_VC_NSI_65                  (Type.STRING, 20, "Вид осуществления контрольной деятельности (НСИ 65)"), // OversightActivitiesRef
        
        // --- PlannedExaminationInfo ---
        OBJECTIVE                       (Type.STRING, 2000, null, "Цель проведения проверки с реквизитами документов основания"),
        CODE_VC_NSI_68                  (Type.STRING, 20, null, "Основание проведения проверки (НСИ 68)"), // Base
        ADDITIONALINFOABOUTEXAMBASE     (Type.STRING, 2000, null, "Дополнительная информация об основаниях проведения проверки"),
        LASTEXAMINATIONENDDATE          (Type.DATE, null, "Дата последнего проведения проверки"),
        MONTHFROM                       (Type.NUMERIC, 2, null, "Месяц начала проверки"),
        YEARFROM                        (Type.NUMERIC, 4, null, "Год начала проведения проверки"),
        WORKDAYS                        (Type.NUMERIC, 25, 4, null, "Срок проведения проверки (рабочих дней)"),
        WORKHOURS                       (Type.NUMERIC, 25, 4, null, "Срок проведения проверки (рабочих часов)"),
        CODE_VC_NSI_71                  (Type.STRING, 20, "Форма проведения проверки (НСИ 71)"), // ExaminationForm
        COOPERATIONWITH                 (Type.STRING, 2048, null, "Орган государственного надзора (контроля) и/или орган муниципального контроля, с которым проверка проводится совместно"),
        PROSECUTORAGREEMENTINFORMATION  (Type.STRING, 2000, null, "Информация о согласовании проведения проверки с органами прокуратуры"),
        
        SUBJECT_LABEL                   (Type.STRING, null, "Наименование субъекта проверки"),
        REGULATOR_LABEL                 (Type.STRING, null, "Наименование регулятора проверки"),
        
        PLANNEDEXAMINATIONGUID          (Type.UUID,        null, "Идентификатор проверки в ГИС ЖКХ"),
        REGISTRYNUMBER                  (Type.STRING, 255, null, "Реестровый номер плана проверок"),
        
        ID_STATUS_GIS                   (VocPlannedExaminationStatus.class, null, "Статус проверки в ГИС ЖКХ"),
        
        CHANGE_CODE_VC_NSI_271          (Type.STRING, 20,       null, "Причина изменения (НСИ 271)"), // ChangeReason
        CHANGEDECISIONNUMBER            (Type.STRING, 255,      null, "Номер решения об изменении"),
        CHANGEDATE                      (Type.DATE,             null, "Дата изменения проверки"),
        CHANGE_ORG_UUID                 (VocOrganization.class, null, "Субъект проверки"),
        ADDITIONCHANGEINFO              (Type.STRING, 2000,     null, "Дополнительная информация"),
        
        CANCEL_CODE_VC_NSI_271          (Type.STRING, 20,       null, "Причина отмены (НСИ 271)"), // Reason
        CANCELDECISIONNUMBER            (Type.STRING, 255,      null, "Номер решения об изменении"),
        CANCELDATE                      (Type.DATE,             null, "Дата изменения проверки"),
        CANCEL_ORG_UUID                 (VocOrganization.class, null, "Субъект проверки"),
        ADDITIONCANCELINFO              (Type.STRING, 2000,     null, "Дополнительная информация"),
        
        ID_LOG                          (PlannedExaminationLog.class, "Последнее событие редактирования")

        ;
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class<?> c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
                case CHECK_PLAN_UUID:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }
        
    }
    
    public PlannedExamination () {
        
        super ("tb_planned_examinations", "Проверки");
        
        cols (c.class);
        
        trigger ("BEFORE UPDATE", ""
                + "BEGIN "
                    + "IF :NEW.subject_uuid IS NOT NULL THEN "
                        + "SELECT org.label "
                        + "INTO :NEW.subject_label "
                        + "FROM vc_orgs org "
                        + "WHERE org.orgrootentityguid = :NEW.subject_uuid; "
                    + "END IF; "
                + "END;"
        );
        
        trigger ("BEFORE INSERT", ""
                + "DECLARE "
                    + "PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "
                    + "FOR i IN (SELECT numberinplan FROM tb_planned_examinations WHERE check_plan_uuid = :NEW.check_plan_uuid AND numberinplan = :NEW.numberinplan AND is_deleted = 0) LOOP "
                        + "raise_application_error (-20000, 'Проверка с №' || i.numberinplan || ' уже существует для этого плана. Операция отменена.'); "
                    + "END LOOP; "
                + "END; "
        );
        
        trigger ("BEFORE INSERT OR UPDATE", ""
                + "BEGIN "
                    + "IF :NEW.regulator_uuid IS NOT NULL THEN "
                        + "SELECT org.label "
                        + "INTO :NEW.regulator_label "
                        + "FROM vc_orgs org "
                        + "WHERE org.orgrootentityguid = :NEW.regulator_uuid; "
                    + "END IF; "
                + "END;"
        );
        
    }
    
}

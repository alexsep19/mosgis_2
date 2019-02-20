define ([], function () {

    var form_name = 'metering_device_common_form'
/*    
    $_DO.approve_metering_device_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'metering_devices', action: 'approve'}, {}, reload_page)
    }
        
    $_DO.alter_metering_device_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'metering_devices', action: 'alter'}, {}, reload_page)
    }    
*/

    $_DO.cancel_metering_device_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'metering_devices'}, {}, function (data) {

            data.__read_only = true
            
            var it = data.item
        
            it.org_label = it ['org.label']           
            if (it.uuid_org_customer) it.label_org_customer = it ['org_customer.label']

            $_F5 (data)

        })

    }
    
    $_DO.edit_metering_device_common = function (e) {

        var data = {item: w2ui [form_name].record}

        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_metering_device_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var it = $('body').data ('data').item
        
        var f = w2ui [form_name]
        var v = f.values ()
        
        if (!v.meteringdevicestamp) die ('meteringdevicestamp', 'Укажите, пожалуйста, марку прибора')
        if (!v.meteringdevicemodel) die ('meteringdevicemodel', 'Укажите, пожалуйста, модель прибора')
        if (!v.meteringdevicenumber) die ('meteringdevicenumber', 'Укажите, пожалуйста, серийный (заводской) номер прибора')
        if (!v.factorysealdate) die ('factorysealdate', 'Укажите, пожалуйста, дата опломбирования прибора заводом-изготовителем')
        
        var is_collective = (it.code_vc_nsi_27 == 2)
        
        if (!is_collective != 2 && !v.commissioningdate) die ('commissioningdate', 'Укажите, пожалуйста, дату ввода прибора в эксплуатацию')
        
        if (v.installationdate && v.commissioningdate && v.installationdate > v.commissioningdate) die ('commissioningdate', 'Дату ввода прибора в эксплуатацию не может предшествовать дате его установки')
        
        if (is_collective) {       
            if (!v.code_vc_nsi_16) die ('code_vc_nsi_16', 'Укажите, пожалуйста, межповерочный интервал')
            if (!v.firstverificationdate) die ('firstverificationdate', 'Укажите, пожалуйста, дату последней поверки')       
        }

        if (v.remotemeteringmode) {       
            if (!v.remotemeteringinfo) {
                if (confirm ('Вы отметили возможность дистанционного снятия показаний, однако не указали наименование системы. Дистанционное снятие показаний всё-таки невозможно?')) {
                    v.remotemeteringmode = 0
                }
                else {
                    die ('remotemeteringinfo', 'Тогда необходимо указать наименование системы')
                }
            }        
        }
        else {        
            v.remotemeteringinfo = null            
        }
        
        if (!v.notlinkedwithmetering) {       
            if (!v.installationplace) die ('installationplace', 'Укажите, пожалуйста, место установки данного прибора')
        }
        else {
            v.installationplace = null
        }

        if (v.temperaturesensor) {       
            if (!v.temperaturesensingelementinfo) die ('temperaturesensingelementinfo', 'Укажите, пожалуйста, место установки датчика температуры')
        }
        else {
            v.temperaturesensingelementinfo = null
        }
                                               
        query ({type: 'metering_devices', action: 'update'}, {data: v}, reload_page)
        
    }
    
    $_DO.delete_metering_device_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'metering_devices', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_metering_device_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('metering_device_common.active_tab', name)
            
        use.block (name)        
    
    }
/*
    function fix (it) {

        it.status_label = $('body').data ('data').vc_gis_status [it.id_ctr_status]            
        it.org_label = it ['org.label']
        
        if (it.ismetering_devicesdivided == null) it.ismetering_devicesdivided = -1
        if (it.isrenter == null) it.isrenter = -1
        
        if (it.uuid_org_customer) it.label_org_customer = it ['org_customer.label']
        
        if (it.uuid_contract) {
            it.label_reason = 'Договор управления №' + it ['ca.docnum'] + ' от '  + dt_dmy (it ['ca.signingdate'])
            it.url_reason = '/mgmt_contract/' + it.uuid_contract
        } 
        
        if (it.uuid_charter) {
            it.label_reason = 'Устав от '  + dt_dmy (it ['ch.date_'])
            it.url_reason = '/charter/' + it.uuid_charter
        } 
        
        it.persons = []
        if (it.uuid_person_customer) {        
            it.persons.push ({
                id: it.uuid_person_customer,
                text: it ['ind_customer.label']
            })            
        }
        
        it.err_text = it ['out_soap.err_text']

    }
*/
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('metering_device_common.active_tab') || 'metering_device_common_log'

        data.__read_only = 1
        
        var it = data.item
        
//        fix (it)
                
        done (data)
        
    }

})
define ([], function () {

    var form_name = 'citizen_compensation_common_form'    

    $_DO.cancel_citizen_compensation_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'citizen_compensations'}, {}, function (data) {

            data.__read_only = true

            var it = data.item            
                        
            $_F5 (data)

        })

    }
    
    $_DO.edit_citizen_compensation_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_citizen_compensation_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
        
        if (!v.uuid_person)      die ('uuid_person', 'Вы забыли указать физлицо')
        if (!v.fiashouseguid)    die ('fiashouseguid', 'Вы забыли указать адрес')
        if (!v.registrationtype) die ('registrationtype', 'Вы забыли указать тип регистрации')

        query ({type: 'citizen_compensations', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_citizen_compensation_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'citizen_compensations', action: 'delete'}, {}, reload_page)
    }
        
    $_DO.approve_citizen_compensation_common = function (e) {

        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return

        query ({type: 'citizen_compensations', action: 'approve'}, {}, reload_page)

    }
    
    $_DO.alter_citizen_compensation_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'citizen_compensations', action: 'alter'}, {}, reload_page)
    }
    
    $_DO.annul_citizen_compensation_common = function (e) {
        if (!confirm ('Запросить аннулирование этого объекта в ГИС ЖКХ?')) return
        query ({type: 'citizen_compensations', action: 'annul'}, {data: {}}, reload_page)
    }
    
    $_DO.create_house_citizen_compensation_common = function (e) {
        use.block ('citizen_compensation_new_house_passport')
    }
    
    $_DO.choose_tab_citizen_compensation_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('citizen_compensation_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('citizen_compensation_common.active_tab') || 'citizen_compensation_common_log'

        data.__read_only = 1
        
        var it = data.item
        
        it.status_label     = data.vc_gis_status [it.id_ctr_status]
        it.err_text = it ['out_soap.err_text']        
            
        done (data)
        
    }

})
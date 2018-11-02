define ([], function () {

    var form_name = 'property_document_common_form'    

    $_DO.cancel_property_document_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'property_documents'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_property_document_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_property_document_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
             
        var p = parseFloat (v.prc)
        if (!(p > 0 && p <= 100)) die ('prc', 'Некорректно указан размер доли')
        
        var now = new Date ().toISOString ()
        
        if (v.dt && v.dt > now) die ('dt', 'Дата документа не может находиться в будущем')
        if (v.dt_to && v.dt_to > now) die ('dt_to', 'Дата прекращения не может находиться в будущем')
        if (v.dt && v.dt_to && v.dt > v.dt_to) die ('dt_to', 'Дата прекращения не может предшествовать дате документа')

        query ({type: 'property_documents', action: 'update'}, {data: v}, reload_page)

    }
      
    
    $_DO.delete_property_document_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'property_documents', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.undelete_property_document_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'property_documents', action: 'undelete'}, {}, reload_page)
    }    
    
    $_DO.choose_tab_property_document_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('property_document_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('property_document_common.active_tab') || 'property_document_common_log'

        data.__read_only = 1
        
        var it = data.item
/*        
        
        it.status_label     = data.vc_gis_status [it.id_ctr_status]
        if (it.id_ctr_status != it.id_ctr_status_gis) it.gis_status_label = data.vc_gis_status [it.id_ctr_status_gis]
        it.startdate = dt_dmy (it.startdate)
        it.enddate = dt_dmy (it.enddate)
        */
//        done (data)
            
        done (data)
        
    }

})
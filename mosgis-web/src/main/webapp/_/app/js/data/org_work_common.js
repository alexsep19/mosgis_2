define ([], function () {

    var form_name = 'org_work_common_form'
    
    $_DO.cancel_org_work_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'org_works'}, {}, function (data) {

            if (!data.item.stringdimensionunit) data.item.stringdimensionunit = $('body').data ('data').vc_okei [data.item.okei]

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_org_work_common = function (e) {

        var data = {item: w2ui [form_name].record}

        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_org_work_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                                        
        if (!v.workname) die ('workname', 'Укажите, пожалуйста, наименование услуги')
        if (!v.code_vc_nsi_56) die ('code_vc_nsi_56', 'Укажите, пожалуйста, вид работ')
        if (!v.stringdimensionunit) die ('stringdimensionunit', 'Укажите, пожалуйста, единицу измерения')
        
        var vc_okei = $('body').data ('data').vc_okei
        for (var id in vc_okei) {
            if (vc_okei [id] != v.stringdimensionunit) continue
            v.okei = id
            delete v.stringdimensionunit
            break
        }
        
        if (!v.okei && !confirm ('Вы уверены, что Общероссийский классификатор единиц измерения неприменим для определения объёма данной услуги?')) return $('#stringdimensionunit').val ('').focus ()
        
        v.code_vc_nsi_67 = w2ui ['code_vc_nsi_67_grid'].getSelection ()
        if (!v.code_vc_nsi_67.length) die ('foo', 'Укажите, пожалуйста, по крайней мере одну обязательную работу')

        query ({type: 'org_works', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_org_work_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'org_works', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.undelete_org_work_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'org_works', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_org_work_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('org_work_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('org_work_common.active_tab') || 'org_work_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']
        
        data.item.status_label = data.vc_async_entity_states [data.item.id_status]
        data.item.err_text = data.item ['out_soap.err_text']
        if (!data.item.stringdimensionunit) data.item.stringdimensionunit = data.vc_okei [data.item.okei]
        
        data.item._can = $_USER.role.admin /*|| data.item.id_status == 10*/ ? {} : {
            edit: 1 - data.item.is_deleted,
            update: 1,
            cancel: 1,
            delete: 1 - data.item.is_deleted,
//            undelete: data.item.is_deleted,
        }

        done (data)
        
    }

})
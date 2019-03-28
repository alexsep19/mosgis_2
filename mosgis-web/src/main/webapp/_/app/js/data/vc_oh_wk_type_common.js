define ([], function () {

    var form_name = 'vc_oh_wk_type_common_form'

    $_DO.edit_vc_oh_wk_type_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false

        var $form = w2ui [form_name]

        $_F5 (data)

    }

    $_DO.cancel_vc_oh_wk_type_common = function (e) {

        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record

        query ({type: 'voc_overhaul_work_types'}, {}, function (data) {

            data.__read_only = true

            var it = data.item

            $_F5 (data)

        })

    }

    $_DO.update_vc_oh_wk_type_common = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        if (!servicename) die ('servicename', 'Укажите, пожалуйста, наименование')
        if (!v.code_vc_nsi_218) die ('code_vc_nsi_218', 'Укажите, пожалуйста, группу работ')

        v.uuid_org = $_USER.uuid_org
                
        var tia = {type: 'voc_overhaul_work_types'}
        tia.id = form.record.uuid
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        query (tia, {data: v}, reload_page)

    }

    $_DO.delete_vc_oh_wk_type_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'voc_overhaul_work_types', action: 'delete'}, {}, reload_page)
    }

    $_DO.alter_vc_oh_wk_type_common = function (e) {
        if (!confirm ('Открыть эту запись на редактирование?')) return
        query ({type: 'voc_overhaul_work_types', action: 'alter'}, {}, reload_page)
    }
    
    $_DO.choose_tab_vc_oh_wk_type_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('vc_oh_wk_type_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('vc_oh_wk_type_common.active_tab') || 'vc_oh_wk_type_common_log'

        data.__read_only = 1

        done (data)
        
    }

})
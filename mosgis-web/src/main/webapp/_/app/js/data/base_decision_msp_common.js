define ([], function () {

    var form_name = 'base_decision_msp_common_form'
    
    $_DO.cancel_base_decision_msp_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'base_decision_msps'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_base_decision_msp_common = function (e) {

        var data = {item: w2ui [form_name].record}

        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_base_decision_msp_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                                
        if (!v.decisionname)                   die ('decisionname', 'Укажите, пожалуйста, наименование')
        if (!v.code_vc_nsi_301)                die ('code_vc_nsi_301', 'Укажите, пожалуйста, тип')
        if (!v.isappliedtosubsidiaries)
            die('isappliedtosubsidiaries', 'Укажите, пожалуйста, применяется для субсидий')
        if (!v.isappliedtorefundofcharges)
            die('isappliedtorefundofcharges', 'Укажите, пожалуйста, применяется для компенсации расходов')

        query ({type: 'base_decision_msps', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_base_decision_msp_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'base_decision_msps', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.alter_base_decision_msp_common = function (e) {
        if (!confirm('Открыть эту запись на редактирование?'))
            return
        query({type: 'base_decision_msps', action: 'alter'}, {}, reload_page)
    }
    
    $_DO.choose_tab_base_decision_msp_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('base_decision_msp_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('base_decision_msp_common.active_tab') || 'base_decision_msp_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']
        
        data.item.status_label = data.vc_gis_status [data.item.id_ctr_status]
        data.item.err_text = data.item ['out_soap.err_text']

        var it = data.item
        var is_locked = it.is_deleted || !$_USER.has_nsi_20(9, 10)

        it._can = {cancel: 1}

        if (!is_locked) {

            switch (it.id_ctr_status) {
                case 11:
                case 14:
                case 34:
                    it._can.edit = 1
            }

            it._can.delete = it._can.update = it._can.edit

            switch (it.id_ctr_status) {
                case 40:
                    it._can.alter = 1
            }

        }

        done (data)
        
    }

})
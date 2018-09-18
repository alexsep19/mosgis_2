define ([], function () {

    var form_name = 'mgmt_contract_common_form'
    
    $_DO.open_orgs_mgmt_contract_common = function (e) {
    
        var f = w2ui [form_name]
            
        function done () {
            f.refresh ()
        }

        $('body').data ('voc_organizations_popup.callback', function (r) {
            
            if (!r) return done ()
            
            query ({type: 'voc_organizations', id: r.uuid, part: 'mgmt_nsi_58'}, {}, function (d) {

                if (!d.vc_nsi_58.length) {
                    alert ('Указанная организация не зарегистрирована в ГИС ЖКХ как возможный заказчик договора управления: ТСЖ, ЖСК и т. п.')
                }
                else {                
                    add_vocabularies (d, d)                                        
                    f.get ('code_vc_nsi_58').options.items = d.vc_nsi_58.items
                    f.record.uuid_org_customer = r.uuid
                    f.record.label_org_customer = r.label                    
                }

                done ()

            })

        })

        use.block ('voc_organizations_popup')

    }

    $_DO.cancel_mgmt_contract_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'mgmt_contracts'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_mgmt_contract_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_mgmt_contract_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                                
        if (!v.docnum) die ('docnum', 'Укажите, пожалуйста, номер договора')
        if (!v.signingdate) die ('signingdate', 'Укажите, пожалуйста, дату заключения договора')
        if (!v.code_vc_nsi_58) die ('code_vc_nsi_58', 'Укажите, пожалуйста, основание заключения договора')

        if (!v.effectivedate) die ('effectivedate', 'Укажите, пожалуйста, дату вступления договора в силу')
        if (v.effectivedate < v.signingdate) die ('effectivedate', 'Дата вступления договора в силу не может предшествовать дате его подписания')

        if (!v.plandatecomptetion) die ('plandatecomptetion', 'Укажите, пожалуйста, плановую дату окончания действия договора')
        if (v.plandatecomptetion < v.effectivedate) die ('plandatecomptetion', 'Дата окончания не может предшествовать дате вступления договора в силу')
                
        if (!v.ddt_m_start) die ('ddt_m_start', 'Укажите, пожалуйста, дату начала ввода показаний ПУ')
        if (!v.ddt_m_end) die ('ddt_m_start', 'Укажите, пожалуйста, дату окончания ввода показаний ПУ')
        
        if (v.ddt_m_start_nxt > v.ddt_m_end_nxt) {
            die ('ddt_m_end_nxt', 'Окончание срока не может предшествовать его началу')
        }        
        else if (v.ddt_m_start_nxt == v.ddt_m_end_nxt) {
            if (v.ddt_m_start > v.ddt_m_end) die ('ddt_m_end', 'Окончание срока не может предшествовать его началу')
        }
        else {
            if (v.ddt_m_start < v.ddt_m_end) die ('ddt_m_end', 'Период сдачи показаний по ИПУ указан некорректно: обнаружено пересечение периодов')
        }
                
        if (!v.ddt_d_start) die ('ddt_d_start', 'Укажите, пожалуйста, срок выставления платёжных документов')
        if (!v.ddt_i_start) die ('ddt_i_start', 'Укажите, пожалуйста, срок внесения платы')

        if (v.ddt_d_start_nxt > v.ddt_i_start_nxt) die ('ddt_i_start_nxt', 'Срок выставления платежных документов не должен превышать срок внесения платы')
        if (v.ddt_d_start_nxt == v.ddt_i_start_nxt) {
            if (v.ddt_d_start > v.ddt_i_start) die ('ddt_i_start', 'Срок выставления платежных документов не должен превышать срок внесения платы')
        }
        
        query ({type: 'mgmt_contracts', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_mgmt_contract_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'mgmt_contracts', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.approve_mgmt_contract_common = function (e) {
        if (!confirm ('Утвердить этот договор, Вы уверены?\n\n(дальнейшая правка станет невозможна)')) return        
        query ({type: 'mgmt_contracts', action: 'approve'}, {}, reload_page)
    }
    
    $_DO.alter_mgmt_contract_common = function (e) {
        if (!confirm ('Открыть этот договор на редактирование, Вы уверены?')) return
        query ({type: 'mgmt_contracts', action: 'alter'}, {}, reload_page)
    }

    $_DO.undelete_mgmt_contract_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'mgmt_contracts', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_mgmt_contract_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('mgmt_contract_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('mgmt_contract_common.active_tab') || 'mgmt_contract_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']
        
        var it = data.item
        
        it.status_label     = data.vc_gis_status [it.id_ctr_status]
        it.state_label      = data.vc_gis_status [it.id_ctr_state]

        if (it.id_ctr_status != 10) {
            if (it.id_ctr_status != it.id_ctr_status_gis) it.gis_status_label = data.vc_gis_status [it.id_ctr_status_gis]
            if (it.id_ctr_state  != it.id_ctr_state_gis ) it.gis_state_label  = data.vc_gis_status [it.id_ctr_state_gis]
        }

        it.err_text = it ['out_soap.err_text']        

        it._can = {}

        if ($_USER.role.nsi_20_1 && !it.is_deleted) {

            switch (it.id_ctr_status) {

                case 10:
                    it._can.approve = 1
                    it._can.delete  = 1
                case 11:
                    it._can.edit    = 1
                    it._can.approve = 1
                    break;
                case 40:
                    if (it.contractguid || it ['out_soap.err_text']) it._can.alter = 1
                    break;

            }

            it._can.update = it._can.cancel = it._can.edit

        }

        done (data)
        
    }

})
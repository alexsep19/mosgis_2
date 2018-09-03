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
        
        if (v.ddt_m_start_nxt > v.ddt_m_end_nxt) die ('ddt_m_end_nxt', 'Окончание срока не может предшествовать его началу')
        
        if (v.ddt_m_start_nxt == v.ddt_m_end_nxt) {
            if (v.ddt_m_start > v.ddt_m_end) die ('ddt_m_end', 'Окончание срока не может предшествовать его началу')
        }
        
        query ({type: 'mgmt_contracts', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_mgmt_contract_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'mgmt_contracts', action: 'delete'}, {}, reload_page)
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
        
        data.item.status_label = data.vc_gis_status [data.item.id_ctr_status]
        data.item.err_text = data.item ['out_soap.err_text']        

        for (i in data.item) {        
            if (!/_nxt/.test (i)) continue            
            if (!data.item [i.substr (0, i.length - 4)]) delete data.item [i]
        }
                
        data.item._can = !$_USER.role.nsi_20_1 /*|| data.item.id_status == 10*/ ? {} : {
            edit: 1 - data.item.is_deleted,
            update: 1,
            cancel: 1,
            delete: 1 - data.item.is_deleted,
//            undelete: data.item.is_deleted,
        }

        done (data)
        
    }

})
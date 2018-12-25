define ([], function () {

    return function (data, view) {
        
        var it = data.item

        $('title').text ('ДПОИ №' + it.contractnumber + ' от ' + dt_dmy (it.date_))        
        
        if (it ['out_soap.err_text']) {
        
            it.sync_label = 'Ошибка передачи в ГИС ЖКХ. Подробности на вкладке "История изменений"'
            
        }
        else if (it.id_ctr_status == 40) {
        
            if (!it.uuid_out_soap) {
            
                it.sync_label = 'Ожидание отправки в ГИС ЖКХ'
            
            }
            else if (!it.contractguid) {
            
                it.sync_label = 'Ожидание ответа от ГИС ЖКХ'
            
            }
        
        }        

        fill (view, it, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'public_property_contract_common',   caption: 'Общие'},
                            {id: 'public_property_contract_docs',     caption: 'Документы'},
                            {id: 'public_property_contract_voting_protocols', caption: 'Протоколы голосования'},
                            {id: 'public_property_contract_agreement_payments', caption: 'Расчеты', off: it.id_ctr_status == 10},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_public_property_contract

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'public_property_contract.active_tab')
            },

        });
        
        clickOn ($('#lnk_address'), function () {openTab ('/house/' + it ['house.uuid'])})
    
        if (it.is_customer_org) {
            clickOn ($('#lnk_customer'), function () {openTab ('/voc_organization_legal/' + it.uuid_org_customer)})
        }
        else {
            clickOn ($('#lnk_customer'), function () {openTab ('/vc_person/' + it.uuid_person_customer)})
        }
        
        clickOn ($('#lnk_org'), function () {openTab ('/voc_organization_legal/' + it.uuid_org)})

    }

})
define ([], function () {

    var form_name = 'mgmt_contract_form'

    $_DO.open_orgs_mgmt_contract_popup = function (e) {
    
        var f = w2ui [form_name]
        
        var saved = {
            data: clone ($('body').data ('data')),
            record: clone (f.record)
        }
        
        function done () {

            $('body').data ('data', saved.data)

            $_SESSION.set ('record', saved.record)

            use.block ('mgmt_contract_popup')

        }
    
        $('body').data ('voc_organizations_popup.callback', function (r) {

            if (!r) return done ()
            
            query ({type: 'voc_organizations', id: r.uuid, part: 'mgmt_nsi_58'}, {}, function (d) {

                var vc_nsi_58 = d.vc_nsi_58

                if (!vc_nsi_58.length) {
                    alert ('Указанная организация не зарегистрирована в ГИС ЖКХ как возможный заказчик договора управления: ТСЖ, ЖСК и т. п.')                    
                }
                else {
                    
                    $.each (vc_nsi_58, function () {
                        this.text = this.label                        
                        if (this ['it.isdefault']) saved.record.code_vc_nsi_58 = this;
                    })
                    
                    saved.record.vc_nsi_58 = vc_nsi_58
                
                    saved.record.uuid_org_customer = r.uuid
                    saved.record.label_org_customer = r.label
                    
                }

                done ()

            })

        })

        use.block ('voc_organizations_popup')

    }

    $_DO.update_mgmt_contract_popup = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        
        if (!v.docnum) die ('docnum', 'Укажите, пожалуйста, номер договора')
        if (!v.signingdate) die ('signingdate', 'Укажите, пожалуйста, дату заключения договора')
        if (!v.code_vc_nsi_58) die ('code_vc_nsi_58', 'Укажите, пожалуйста, основание заключения договора')

        if (!v.effectivedate) die ('effectivedate', 'Укажите, пожалуйста, дату вступления договора в силу')
        if (v.effectivedate < v.signingdate) die ('effectivedate', 'Дата вступления договора в силу не может предшествовать дате его подписания')

        if (!v.plandatecomptetion) die ('plandatecomptetion', 'Укажите, пожалуйста, плановую дату окончания действия договора')
        if (v.plandatecomptetion < v.effectivedate) die ('plandatecomptetion', 'Дата окончания не может предшествовать дате вступления договора в силу')
        
        var tia = {type: 'mgmt_contracts'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['mgmt_contracts_grid']

        query (tia, {data: v}, function () {
        
            w2popup.close ()
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var r = $_SESSION.delete ('record')
        
        if (!('automaticrolloveroneyear' in r)) r.automaticrolloveroneyear = '0'        
        
        if (!r.label_org_customer) r.label_org_customer = 'Собственники объекта жилищного фонда'

        if (!r.vc_nsi_58) r.vc_nsi_58 = data.vc_nsi_58.items.filter (function (i) {return 'it.isdefault' in i})
        
        data.record = r

        done (data)

    }

})
define ([], function () {

    var form_name = 'public_property_contract_form'

    $_DO.open_orgs_public_property_contract_org_new = function (e) {
    
        var f = w2ui [form_name]
        
        var saved = {
            data: clone ($('body').data ('data')),
            record: clone (f.record)
        }
        
        function done () {
            $('body').data ('data', saved.data)
            $_SESSION.set ('record', saved.record)
            use.block ('public_property_contract_org_new')
        }
    
        $('body').data ('voc_organizations_popup.callback', function (r) {

            if (!r) return done ()
            
            saved.record.uuid_org_customer = r.uuid
            saved.record.label_org_customer = r.label
                
            done ()

        })

        use.block ('voc_organizations_popup')

    }

    $_DO.update_public_property_contract_org_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        var r = form.record
        
        if (!(v.uuid_org_customer = r.uuid_org_customer)) die ('label_org_customer', 'Вы забыли указать заказчика')
        if (!v.fiashouseguid) die ('fiashouseguid', 'Вы забыли указать МКД')
        
        if (!v.contractnumber) die ('contractnumber', 'Вы забыли указать номер договора')
        if (!v.date_) die ('date_', 'Вы забыли указать дату договора')
        if (v.date_ > new Date ().toISOString ()) die ('date_', 'Дата договора не может находиться в будущем')
        
        if (!v.startdate) die ('startdate', 'Вы забыли указать дату начала действия договора')
        if (!v.enddate) die ('enddate', 'Вы забыли указать предполагаемую дату окончания действия договора')

        if (v.startdate > v.enddate) die ('enddate', 'Дата окончания не может предшествовать дате начала')
        
        query ({type: 'public_property_contracts', id: undefined, action: 'create'}, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Договор зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/public_property_contract/' + data.id)})
            
            var grid = w2ui ['public_property_contracts_grid']
            
            grid.reload (grid.refresh)            
        
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var r = $_SESSION.delete ('record')
                
        data.record = r || {}
        
        if (!data.record.uuid_org_customer) return done (data)
        
        query ({type: 'houses', id: undefined}, {limit:100000, offset:0, search: [{field:"is_condo",operator:"is",value:1}], searchLogic: "AND"}, function (d) {
        
            data.houses = d.tb_houses.map (function (i) {return {
                id: i.fiashouseguid, 
                text: i.address
            }})

            done (data)

        })

    }

})
define ([], function () {

    var form_name = 'public_property_contract_form'

    $_DO.update_public_property_contract_person_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        
        if (!v.uuid_person_customer) die ('uuid_person_customer', 'Вы забыли указать заказчика')
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
                        
        data.record = {}

        query ({type: 'vc_persons', id: undefined}, {limit:100000, offset:0}, function (d) {

            data.persons = d.root.map (function (i) {return {
                id: i.id, 
                text: i.label
            }})

            query ({type: 'houses', id: undefined}, {limit:100000, offset:0, search: [{field:"is_condo",operator:"is",value:1}], searchLogic: "AND"}, function (d) {

                data.houses = d.tb_houses.map (function (i) {return {
                    id: i.fiashouseguid, 
                    text: i.address
                }})

                done (data)

            })

        })
        
                

    }

})
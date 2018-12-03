define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'public_property_contract_form',

                record: data.record,

                fields : [                                

                    {name: 'contractnumber', type: 'text'},
                    {name: 'label_org_customer', type: 'text'},

                    {name: 'date_', type: 'date'},
                    {name: 'enddate', type: 'date'},
                    {name: 'startdate', type: 'date'},
                    
                    {name: 'fiashouseguid', type: 'text'},

                ],
                
                focus: 2,
                
                onRefresh: function (e) {e.done (function () {
                
                    clickOn ($('#label_org_customer'), $_DO.open_orgs_public_property_contract_org_new)
                
                })}                

            })
            
            if (!data.record.uuid_org_customer) $('#label_org_customer').click ()

       })

    }

})
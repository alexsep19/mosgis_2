define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'account_org_new_form',

                record: data.record,

                fields : [  
                
                    {name: 'accountnumber', type: 'text'},
                    {name: 'label_org_customer', type: 'text'},
                    {name: 'uuid_org_customer', type: 'hidden'},
                    
                    {name: 'isaccountsdivided', type: 'list', options: {items: [
                        {id: -1, text: '[нет данных]'},
                        {id:  0, text: 'нет, не разделен(ы)'},
                        {id:  1, text: 'да, разделен(ы)'},
                    ]}},
                    
                    {name: 'isrenter', type: 'list', options: {items: [
                        {id: -1, text: '[нет данных]'},
                        {id:  0, text: 'нет, не является нанимателем'},
                        {id:  1, text: 'да, является нанимателем'},
                    ]}},
                    
                    {name: 'totalsquare', type: 'float', options: {min: 0, precision: 2}},
                    
                ],
                
                onRefresh: function (e) {e.done (function () {
                    clickOff ($('#label_org_customer'))
                    clickOn ($('#label_org_customer'), $_DO.open_orgs_account_org_new)
                })}                

            })                       

       })

    }

})
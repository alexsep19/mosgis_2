define ([], function () {

    return function (data, view) {
    
        var house = data.item
        
        var min_year = house.usedyear || 1600

        $(view).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'lift_popup_form',

                record: data.lift,

                fields : [                
                
                    {name: 'uuid_entrance', type: 'list', options: {
                        items: data.entrances.items
                    }},
                    {name: 'code_vc_nsi_192', type: 'list', options: {
                        items: data.vc_nsi_192.items
                    }},
                    {name: 'factorynum', type: 'text'},
                    {name: 'f_20007', type: 'text'},
                    {name: 'f_20165', type: 'float'},
                    {name: 'f_20164', type: 'int', options: {
//                        min: min_year,
                        max: (new Date ()).getFullYear (), 
                        autoFormat: false
                    }},
                    {name: 'f_20166', type: 'int'},
                    {name: 'operatinglimit', type: 'int'},
                    {name: 'f_20151', type: 'float'},
                    {name: 'f_20124', type: 'int', options: {
                        min: min_year,
                        max: (new Date ()).getFullYear (), 
                        autoFormat: false
                    }},
                    {name: 'terminationdate', type: 'date', options: {
                        min: new Date(min_year, 1, 1, 0, 0, 0, 0), 
                        max: (new Date ())
                    }},
                    {name: 'annulmentinfo', type: 'text'},
                
                ],
                
                focus: -1,

            })

            clickOn ($('#w2ui-popup button'), $_DO.update_lift_popup)

       })

    }

})
define ([], function () {

    return function (data, view) {

        $(fill (view, {})).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'legal_acts_import',

                fields : [
                    {name: 'level_', type: 'list', options: {
                            items: data.vc_legal_act_levels.items.filter((i) => {return i.id != 3})
                    }},
                    {name: 'acceptstartdate', type: 'date'},
                    {name: 'acceptenddate',   type: 'date'}
                ],

            })
            
       })       

    }

})
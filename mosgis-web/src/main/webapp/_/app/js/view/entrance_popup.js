define ([], function () {

    return function (data, view) {
    
        var house = JSON.parse (JSON.stringify ($('body').data ('data').item))
        
        if (!house.usedyear) house.usedyear = 1600
        
        if (!house.minfloorcount) house.minfloorcount = house.floorcount    

        $(view).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'entrance_popup_form',

                record: data.entrance,

                fields : [                
                    {name: 'entrancenum', type: 'text'},
                    {name: 'storeyscount', type: 'int', options: {
                        min: house.minfloorcount, 
                        max: house.floorcount
                    }},
                    {name: 'creationyear', type: 'int', options: {
                        min: house.usedyear, 
                        max: (new Date ()).getFullYear (), 
                        autoFormat: false
                    }},
                ],
                
//                focus: 1,

            })

            clickOn ($('#w2ui-popup button'), $_DO.update_entrance_popup)

       })

    }

})
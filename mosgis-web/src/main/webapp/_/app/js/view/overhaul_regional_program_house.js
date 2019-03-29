define ([], function () {

    return function (data, view) {
        
        console.log (data)

        var it = data.item

        $('title').text ('Дом по адресу ' + it.address + ', включенный в РПКР ' + it.startyear + '-' + it.endyear)
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'overhaul_regional_program_house_common', caption: 'Общие'},
                            {id: 'overhaul_regional_program_house_works', caption: 'Виды работ'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_overhaul_regional_program_house

                    }                

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'overhaul_regional_program_house.active_tab')
            },

        });
            
    }

})
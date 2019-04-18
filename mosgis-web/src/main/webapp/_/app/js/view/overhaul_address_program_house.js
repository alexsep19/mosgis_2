define ([], function () {

    return function (data, view) {
        
        console.log (data)

        var it = data.item

        $('title').text ('Дом по адресу ' + it.address + ', включенный в адресную программу ' + it.start + '-' + it.end)
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'overhaul_address_program_house_common', caption: 'Общие'},
                            //{id: 'overhaul_address_program_house_works', caption: 'Виды работ'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_overhaul_address_program_house

                    }                

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'overhaul_address_program_house.active_tab')
            },

        });

        $(('#address_program_link')).attr({title: 'Перейти на страницу адресной программы'})
        clickOn ($('#address_program_link'), function () { openTab ('/overhaul_address_program/' + data.item.program_uuid) })
            
    }

})
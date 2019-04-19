define ([], function () {

    return function (data, view) {
        
        var it = data.item

        $('title').text ('Адресная программа капитального ремонта ' + it.start + '-' + it.end)
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'overhaul_address_program_common', caption: 'Программа'},
                            //{id: 'overhaul_address_program_houses', caption: 'Дома и виды работ'},
                            {id: 'overhaul_address_program_docs', caption: 'Документы'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_overhaul_address_program

                    }                

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'overhaul_address_program.active_tab')
            },

        });
            
    }

})
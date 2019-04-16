define ([], function () {

    return function (data, view) {
        
        var it = data.item

        $('title').text ('Краткосрочная программа капитального ремонта ' + it.start + '-' + it.end)
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'overhaul_short_program_common', caption: 'Программа'},
                            //{id: 'overhaul_short_program_houses', caption: 'Дома и виды работ'},
                            {id: 'overhaul_short_program_docs', caption: 'Документы'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_overhaul_short_program

                    }                

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'overhaul_short_program.active_tab')
            },

        });
            
    }

})
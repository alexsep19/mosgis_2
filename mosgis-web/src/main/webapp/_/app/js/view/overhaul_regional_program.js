define ([], function () {

    return function (data, view) {
        
        var it = data.item

        $('title').text ('Региональная программа капитального ремонта ' + it.startyear + '-' + it.endyear)
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'overhaul_regional_program_common', caption: 'Программа'},
                            {id: 'overhaul_regional_program_docs', caption: 'Документы'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_overhaul_regional_program

                    }                

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'overhaul_regional_program.active_tab')
            },

        });
            
    }

})
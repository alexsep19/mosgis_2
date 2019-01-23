define ([], function () {

    $_DO.choose_tab_infrastructure = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('infrastructure.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'infrastructures', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            var ref_33_to_3 = []

            data.ref_33_to_3.items.forEach ((x, index, arr) => {

                if (ref_33_to_3.findIndex (y => {
                    return y.code_33 == x.code_33
                }) < 0) {

                    ref_33_to_3.push (data.ref_33_to_3.items.filter (z => z.code_33 == x.code_33).reduce ((accumulator, currentValue, currentIndex, array) => {
                        accumulator.code_3.push (data.vc_nsi_3.items.find (nsi_3 => nsi_3.id == currentValue.code_3))
                        return accumulator
                    }, {code_33: x.code_33, code_3: []}))

                }

            })

            data.ref_33_to_3 = ref_33_to_3

            query ({type: 'infrastructures'}, {}, function (d) {

                var it = data.item = d.item

                $('body').data ('data', data)

                done (data)

            })

        })

    }

})